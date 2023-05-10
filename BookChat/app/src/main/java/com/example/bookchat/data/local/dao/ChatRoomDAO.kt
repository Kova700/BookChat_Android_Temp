package com.example.bookchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.bookchat.data.local.entity.ChatRoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDAO {

    @Query("SELECT * FROM ChatRoom " +
            "ORDER BY top_pin_num DESC, last_chat_id DESC")
    fun pagingSource(): PagingSource<Int, ChatRoomEntity>

    @Query("SELECT * FROM ChatRoom " +
            "ORDER BY top_pin_num DESC, last_chat_id DESC " +
            "LIMIT :loadSize")
    fun getChatRoom(loadSize: Int): Flow<List<ChatRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(chatRoom : ChatRoomEntity) :Long

    suspend fun insertOrUpdateAllChatRoom(chatRooms: List<ChatRoomEntity>){
        for (chatRoom in chatRooms) { insertOrUpdateChatRoom(chatRoom) }
    }

    suspend fun insertOrUpdateChatRoom(chatRoom: ChatRoomEntity) {
        val id = insertIgnore(chatRoom)
        if (id != -1L) return

        updateForInsert(
            roomId = chatRoom.roomId,
            roomName = chatRoom.roomName,
            roomSid = chatRoom.roomSid,
            roomMemberCount = chatRoom.roomMemberCount,
            defaultRoomImageType = chatRoom.defaultRoomImageType,
            roomImageUri = chatRoom.roomImageUri,
            lastChatId = chatRoom.lastChatId,
            lastActiveTime = chatRoom.lastActiveTime,
            lastChatContent = chatRoom.lastChatContent,
        )
    }

    @Query("UPDATE ChatRoom SET " +
            "room_name = :roomName, " +
            "room_socket_id = :roomSid, " +
            "room_member_count = :roomMemberCount, " +
            "default_room_image_type = :defaultRoomImageType, " +
            "room_image_uri = :roomImageUri, " +
            "last_chat_id = :lastChatId, " +
            "last_active_time = :lastActiveTime, " +
            "last_chat_content = :lastChatContent " +
            "WHERE room_id = :roomId")
    suspend fun updateForInsert(
        roomId: Long,
        roomName: String,
        roomSid: String,
        roomMemberCount: Long,
        defaultRoomImageType: Int,
        roomImageUri: String?,
        lastChatId: Long?,
        lastActiveTime: String?,
        lastChatContent: String?
    )

    @Query("UPDATE ChatRoom SET " +
            "last_chat_id = :lastChatId, " +
            "last_active_time = :lastActiveTime, " +
            "last_chat_content = :lastChatContent " +
            "WHERE room_id = :roomId")
    suspend fun updateLastChatInfo(
        roomId: Long,
        lastChatId: Long,
        lastActiveTime: String,
        lastChatContent: String
    )

    @Query("SELECT MAX(top_pin_num) FROM ChatRoom")
    suspend fun getMaxPinNum() :Int?
}