package com.example.bookchat.data.response

import com.example.bookchat.data.ChatRoomListItem
import com.google.gson.annotations.SerializedName

data class ResponseGetChatRoomList(
    @SerializedName("chatRoomResponseList")
    val chatRoomList :List<ChatRoomListItem>,
    @SerializedName("cursorMeta")
    val cursorMeta : CursorMeta
)