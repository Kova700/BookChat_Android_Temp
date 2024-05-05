package com.example.bookchat.data.repository

import android.util.Log
import com.example.bookchat.data.database.dao.ChannelDAO
import com.example.bookchat.data.mapper.toBookRequest
import com.example.bookchat.data.mapper.toChannel
import com.example.bookchat.data.mapper.toChannelDefaultImageTypeNetwork
import com.example.bookchat.data.mapper.toChannelEntity
import com.example.bookchat.data.network.BookChatApi
import com.example.bookchat.data.network.model.response.ResponseChannelInfo
import com.example.bookchat.domain.model.Book
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.repository.ChannelRepository
import com.example.bookchat.domain.repository.ChatRepository
import com.example.bookchat.domain.repository.UserRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.toMultiPartBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

//TODO : 채팅방 정보 조회 API 실패 시 재시도 로직 필요함 (채팅 전송 재시도 로직같은)
//TODO : 채팅방 사전에 전부 로드해오는 로직 추가필요 ( FCM 통해 받은 채팅 저장 하기 위해서  OR FCM 통해 받은 채팅의 채팅방 정보 보여주기 위해)
//TODO : 채널 ID, 메세지 ID를 FCM을 통해 받고 추가 정보를 API로 요청 후 Noti를 띄우는 방식하면 전부 가져오지 않아도 됨
class ChannelRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val channelDAO: ChannelDAO,
	private val userRepository: UserRepository,
	private val chatRepository: ChatRepository
) : ChannelRepository {

	private val mapChannels = MutableStateFlow<Map<Long, Channel>>(emptyMap())//(channelId, Channel)
	private val channels = mapChannels.map {
		//ORDER BY top_pin_num DESC, last_chat_id DESC, room_id DESC
		it.values.toList().sortedWith(
			compareBy(
				{ channel -> -channel.topPinNum },
				{ channel -> channel.lastChat?.chatId?.unaryMinus() },
				{ channel -> -channel.roomId })
		)
	}.onEach { cachedChannels = it }
	private var cachedChannels: List<Channel> = emptyList()
	private var currentPage: Long? = null
	private var isEndPage = false

	//TODO : 상태 clear 함수 필요
	override fun getChannelsFlow(): Flow<List<Channel>> {
		return channels
	}

	override fun getChannelFlow(channelId: Long): Flow<Channel> {
		return mapChannels.map { it[channelId] }.filterNotNull().distinctUntilChanged()
	}

	private fun setChannels(newChannels: Map<Long, Channel>) {
		mapChannels.update { newChannels }
	}

	override suspend fun getChannel(channelId: Long): Channel {
		getChannelInfo(channelId)
		val updatedChannel = getChannelWithInfo(channelId)
		setChannels(mapChannels.value + (channelId to updatedChannel))
		return updatedChannel
	}

	//Channel 세부 정보는 채팅방 들어 가면 getChannel에 의해 갱신될 예정
	override suspend fun getChannels(loadSize: Int): List<Channel> {
		if (isEndPage) return cachedChannels

		val response = bookChatApi.getChannels(
			postCursorId = currentPage,
			size = loadSize
		)
		isEndPage = response.cursorMeta.last
		currentPage = response.cursorMeta.nextCursorId

		channelDAO.upsertAllChannels(response.channels.toChannelEntity())
		val chats = response.channels.mapNotNull { it.lastChat }
		chatRepository.insertAllChats(chats)
		val newChannels = response.channels.map { getChannelWithInfo(it.roomId) }
		setChannels(mapChannels.value + newChannels.associateBy { it.roomId })
		return newChannels //반환 값에 이전값이 포함되지 않고 있음
	}

	//DB에 저장된 LastChat과, 기존에 저장되어있던 채널 정보들을 묶어서 반환 (like topPin..)
	private suspend fun getChannelWithInfo(channelId: Long): Channel {
		val channel =
			channelDAO.getChannel(channelId)?.toChannel() ?: Channel.DEFAULT.copy(roomId = channelId)
		return channel.copy(
			lastChat = channel.lastChat?.chatId?.let { chatRepository.getChat(it) },
			host = channel.host?.id?.let { userRepository.getUser(it) },
			subHosts = channel.subHosts?.map { user -> userRepository.getUser(user.id) },
			guests = channel.guests?.map { user -> userRepository.getUser(user.id) }
		)
	}

	override suspend fun makeChannel(
		channelTitle: String,
		channelSize: Int,
		defaultRoomImageType: ChannelDefaultImageType,
		channelTags: List<String>,
		selectedBook: Book,
		channelImage: ByteArray?
	): Channel {
		val response = bookChatApi.makeChannel(
			requestMakeChannel = com.example.bookchat.data.network.model.request.RequestMakeChannel(
				roomName = channelTitle,
				roomSize = channelSize,
				defaultRoomImageType = defaultRoomImageType.toChannelDefaultImageTypeNetwork(),
				hashTags = channelTags,
				bookRequest = selectedBook.toBookRequest()
			),
			chatRoomImage = channelImage?.toMultiPartBody(
				contentType = CONTENT_TYPE_IMAGE_WEBP,
				multipartName = IMAGE_MULTIPART_NAME,
				fileName = IMAGE_FILE_NAME,
				fileExtension = IMAGE_FILE_EXTENSION_WEBP
			)
		)

		val createdChannelId = response.headers()["Location"]?.split("/")?.last()?.toLong()
			?: throw Exception("ChannelId does not exist in Http header.")

		val createdChannel = getChannel(createdChannelId)
		channelDAO.upsertChannel(createdChannel.toChannelEntity())
		setChannels(mapChannels.value + mapOf(Pair(createdChannel.roomId, createdChannel)))
		return createdChannel
	}

	// TODO : 이미 입장되어있는 채널에 입장 API 호출하면 넘어오는 응답코드 따로 정의 후,
	//  해당 코드 응답시, 예외 던지기
	override suspend fun enter(channel: Channel) {
		val resultCode = bookChatApi.enterChatRoom(channel.roomId).code()
		Log.d(TAG, "ChannelRepositoryImpl: enter() - resultCode : $resultCode")

		val newChannel = channel.toChannelEntity()
			.copy(lastChatId = Long.MAX_VALUE)
		channelDAO.upsertChannel(newChannel)
		setChannels(mapChannels.value + mapOf(Pair(channel.roomId, channel)))
	}

	override suspend fun isAlreadyEntered(channelId: Long): Boolean {
		return channelDAO.isExist(channelId)
	}

	override suspend fun leave(channelId: Long) {
		bookChatApi.leaveChatRoom(channelId)
		channelDAO.delete(channelId)
		setChannels(mapChannels.value - channelId)
	}

	private suspend fun getChannelInfo(roomId: Long) {
		val response = bookChatApi.getChatRoomInfo(roomId)
		saveParticipantsDataInLocalDB(response)
		saveChannelInfoInLocalDB(roomId, response)
	}

	override suspend fun updateMemberCount(channelId: Long, offset: Int) {
		channelDAO.updateMemberCount(channelId, offset)
	}

	override suspend fun updateLastChat(channelId: Long, chatId: Long) {
		val existingLastChatId = channelDAO.getChannel(channelId)?.toChannel()?.lastChat?.chatId
		if (existingLastChatId != null && chatId <= existingLastChatId) return

		channelDAO.updateLastChat(
			roomId = channelId,
			lastChatId = chatId,
		)

		val updatedChannel = getChannelWithInfo(channelId)
		setChannels(mapChannels.value + (channelId to updatedChannel))
	}

	private suspend fun saveParticipantsDataInLocalDB(chatRoomInfo: ResponseChannelInfo) {
		userRepository.upsertAllUsers(chatRoomInfo.participants)
	}

	private suspend fun saveChannelInfoInLocalDB(roomId: Long, channelInfo: ResponseChannelInfo) {
		channelDAO.updateDetailInfo(
			roomId = roomId,
			hostId = channelInfo.roomHost.id,
			roomName = channelInfo.roomName,
			subHostIds = channelInfo.roomSubHostList?.map { it.id },
			guestIds = channelInfo.roomGuestList?.map { it.id },
			bookTitle = channelInfo.bookTitle,
			bookAuthors = channelInfo.bookAuthors,
			bookCoverImageUrl = channelInfo.bookCoverImageUrl,
			roomTags = channelInfo.roomTags,
			roomCapacity = channelInfo.roomCapacity,
		)
	}

	companion object {
		private const val CONTENT_TYPE_IMAGE_WEBP = "image/webp"
		private const val IMAGE_FILE_NAME = "profile_img"
		private const val IMAGE_FILE_EXTENSION_WEBP = ".webp"
		private const val IMAGE_MULTIPART_NAME = "chatRoomImage"
	}

}