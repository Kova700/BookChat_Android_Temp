package com.kova700.bookchat.core.data.user.internal

import com.kova700.bookchat.core.data.user.external.model.User
import com.kova700.bookchat.core.data.user.external.repository.UserRepository
import com.kova700.bookchat.core.database.chatting.external.user.UserDAO
import com.kova700.bookchat.core.network.bookchat.BookChatApi
import toUser
import toUserEntity
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
	private val bookChatApi: BookChatApi,
	private val userDao: UserDAO,
) : UserRepository {

	/** 로컬에 있는 유저 우선적으로 쿼리 */
	override suspend fun getUser(userId: Long): User {
		val offlineData = userDao.getUser(userId)?.toUser()
		if (offlineData != null) return offlineData

		val response = runCatching { bookChatApi.getUser(userId).toUser() }
		if (response.isFailure) return User.Default.copy(nickname = "알 수 없음") // 탈퇴한 유저 혹은 잘못된 요청

		userDao.upsertUser(response.getOrThrow().toUserEntity())
		return response.getOrThrow()
	}

	override suspend fun upsertAllUsers(users: List<User>) {
		userDao.upsertAllUsers(users.toUserEntity())
	}

	override suspend fun clear() {
		userDao.deleteAll()
	}

}