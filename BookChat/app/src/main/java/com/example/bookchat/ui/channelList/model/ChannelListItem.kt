package com.example.bookchat.ui.channelList.model

import com.example.bookchat.domain.model.Channel
import com.example.bookchat.domain.model.ChannelDefaultImageType
import com.example.bookchat.domain.model.Chat
import com.example.bookchat.domain.model.User

sealed interface ChannelListItem {
	fun getCategoryId(): Long {
		return when (this) {
			Header -> CHANNEL_HEADER_ITEM_STABLE_ID
			is ChannelItem -> roomId
		}
	}

	object Header : ChannelListItem

	data class ChannelItem(
		val roomId: Long,
		val roomName: String,
		val roomSid: String,
		val roomMemberCount: Long,
		val defaultRoomImageType: ChannelDefaultImageType,
		val notificationFlag: Boolean = true,
		val topPinNum: Int = 0,
		val roomImageUri: String? = null,
		val lastChat: Chat? = null,
		val host: User? = null,
		val subHosts: List<User>? = null,
		val guests: List<User>? = null,
		val roomTags: List<String>? = null,
		val roomCapacity: Int? = null,
		val bookTitle: String? = null,
		val bookAuthors: List<String>? = null,
		val bookCoverImageUrl: String? = null,
	) : ChannelListItem {
		val bookAuthorsString
			get() = bookAuthors?.joinToString(",")

		val tagsString
			get() = roomTags?.joinToString(",")

		val participants
			get() = mutableListOf<User>().apply {
				host?.let { add(it) }
				subHosts?.let { addAll(it) }
				guests?.let { addAll(it) }
			}.toList()

		val participantIds
			get() = mutableListOf<Long>().apply {
				host?.id?.let { add(it) }
				subHosts?.map { it.id }?.let { addAll(it) }
				guests?.map { it.id }?.let { addAll(it) }
			}.toList()

		companion object {
			val DEFAULT = Channel(
				roomId = 0L,
				roomName = "",
				roomSid = "",
				roomMemberCount = 0,
				defaultRoomImageType = ChannelDefaultImageType.ONE
			)
		}
	}

	companion object {
		const val CHANNEL_HEADER_ITEM_STABLE_ID = -1L
	}
}