package com.example.bookchat.api

import com.example.bookchat.data.*
import com.example.bookchat.utils.BookSearchSortOption
import com.example.bookchat.utils.ReadingStatus
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BookApiInterface {
    //API 테스트
    //@POST("/3862dd02-166b-4975-9a3c-1666ebd4fcfa")

    @GET("/v1/api/books")
    suspend fun getBookFromTitle(
        @Query("query") query:String,
        @Query("size") size:String,
        @Query("page") page:String,
        @Query("sort") sort: BookSearchSortOption = BookSearchSortOption.ACCURACY,
    ): Response<BookSearchResult>

    @POST("/v1/api/bookshelf/books")
    suspend fun registerWishBook(
        @Body requestRegisterWishBook: RequestRegisterWishBook
    ):Response<Unit>

    @POST("/v1/api/bookshelf/books")
    suspend fun registerReadingBook(
        @Body requestRegisterReadingBook: RequestRegisterReadingBook
    ):Response<Unit>

    @POST("/v1/api/bookshelf/books")
    suspend fun registerCompleteBook(
        @Body requestRegisterCompleteBook: RequestRegisterCompleteBook
    ):Response<Unit>

    @GET("/v1/api/bookshelf/books")
    suspend fun getWishBooks(
        @Query("size") size:String = "21",
        @Query("page") page:String = "0",
        @Query("sort") sort: String = "id,DESC",
        @Query("readingStatus") readingStatus : ReadingStatus = ReadingStatus.WISH
    ):Response<BookShelfResult>
}