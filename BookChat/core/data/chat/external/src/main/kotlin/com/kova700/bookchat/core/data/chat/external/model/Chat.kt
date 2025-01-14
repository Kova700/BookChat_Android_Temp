package com.kova700.bookchat.core.data.chat.external.model

import com.kova700.bookchat.core.data.user.external.model.User

data class Chat(
	val chatId: Long,
	val channelId: Long,
	val message: String,
	val state: ChatState,
	val dispatchTime: String,
	val sender: User?,
) {
	fun getChatType(clientId: Long): ChatType {
		return when (sender?.id) {
			clientId -> ChatType.Mine
			null -> ChatType.Notice
			else -> ChatType.Other
		}
	}

	companion object {
		val DEFAULT = Chat(
			chatId = -1,
			channelId = -1,
			message = "",
			state = ChatState.SUCCESS,
			dispatchTime = "",
			sender = null
		)
	}
}