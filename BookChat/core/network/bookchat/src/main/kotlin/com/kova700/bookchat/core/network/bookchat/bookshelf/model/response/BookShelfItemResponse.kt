package com.kova700.bookchat.core.network.bookchat.bookshelf.model.response

import com.kova700.bookchat.core.data.bookshelf.external.model.StarRating
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookShelfItemResponse(
	@SerialName("bookShelfId")
	val bookShelfId: Long,
	@SerialName("title")
	val title: String,
	@SerialName("isbn")
	val isbn: String,
	@SerialName("bookCoverImageUrl")
	val bookCoverImageUrl: String,
	@SerialName("authors")
	val authors: List<String>,
	@SerialName("publisher")
	val publisher: String,
	@SerialName("publishAt")
	val publishAt: String,
	@SerialName("pages")
	val pages: Int? = 0, //TODO : 서버 수정을 위해 임시로 nullable 처리 + default value 추가
	@SerialName("star")
	val star: StarRating?,
	@SerialName("lastUpdatedAt")
	val lastUpdatedAt: String,
)