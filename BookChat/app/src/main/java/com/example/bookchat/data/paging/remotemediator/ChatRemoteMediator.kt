package com.example.bookchat.data.paging.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.bookchat.data.api.BookChatApiInterface
import com.example.bookchat.data.local.BookChatDB
import com.example.bookchat.data.local.entity.ChatEntity
import com.example.bookchat.data.local.entity.ChatWithUser

@OptIn(ExperimentalPagingApi::class)
class ChatRemoteMediator(
    private val chatRoomId: Long,
    private val database: BookChatDB,
    private val apiClient: BookChatApiInterface
) : RemoteMediator<Int, ChatWithUser>() {

    private var isLast = false
    private var isFirst = true

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatWithUser>
    ): MediatorResult {

        val loadKey = when (loadType) {
            LoadType.REFRESH -> null
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = isLast)
                lastItem.chat.chatId
            }
        }

        return try {
            val response = apiClient.getChat(
                roomId = chatRoomId,
                size = getLoadSize(),
                postCursorId = loadKey
            )

            val result = response.body()
            result?.let {
                val pagedChatList = result.chatResponseList.map { it.toChatEntity(chatRoomId) }
                val meta = result.cursorMeta
                saveChatInLocalDB(pagedChatList)
                isLast = meta.last
                isFirst = false
            }

            MediatorResult.Success(endOfPaginationReached = isLast)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private fun getLoadSize(): Int = if (isFirst) 3 * CHAT_LOAD_SIZE else CHAT_LOAD_SIZE

    private suspend fun saveChatInLocalDB(pagedList: List<ChatEntity>) {
        database.withTransaction {
            database.chatDAO().insertAllChat(pagedList)

            if (isFirst) {
                val lastChat = pagedList.firstOrNull() ?: return@withTransaction
                database.chatRoomDAO().updateLastChatInfo(
                    roomId = chatRoomId,
                    lastChatId = lastChat.chatId,
                    lastActiveTime = lastChat.dispatchTime,
                    lastChatContent = lastChat.message
                )
            }
        }
    }

    override suspend fun initialize(): InitializeAction {
        return super.initialize()
    }

    companion object {
        private const val CHAT_LOAD_SIZE = 25
    }
}
