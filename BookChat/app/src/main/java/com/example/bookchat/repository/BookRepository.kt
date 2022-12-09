package com.example.bookchat.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.bookchat.App
import com.example.bookchat.data.*
import com.example.bookchat.paging.SearchResultBookDetailPagingSource
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.response.ResponseBodyEmptyException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Singleton
class BookRepository {

    suspend fun simpleSearchBooks(keyword :String) : BookSearchResult {
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.getBookSearchResult(
            query = keyword,
            size = SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE.toString(),
            page = 1.toString()
        )

        when(response.code()){
            200 -> {
                val bookSearchResult = response.body()
                bookSearchResult?.let { return bookSearchResult }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }

    }

    fun detailSearchBooks(keyword :String) : Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig( pageSize = SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE, enablePlaceholders = false ),
            pagingSourceFactory = { SearchResultBookDetailPagingSource(keyword) }
        ).flow
    }

    suspend fun registerBookShelfBook(requestRegisterBookShelfBook: RequestRegisterBookShelfBook){
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookApiInterface.registerBookShelfBook(requestRegisterBookShelfBook)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun deleteBookShelfBook(bookId :Long){
        Log.d(TAG, "BookRepository: deleteBookShelfBook() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.deleteBookShelfBook(bookId)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun changeBookShelfBookStatus(book :BookShelfItem, readingStatus : ReadingStatus){
        Log.d(TAG, "BookRepository: changeBookShelfBookStatus() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val requestBody = RequestChangeBookStatus(
            readingStatus = readingStatus,
            star = book.star,
            pages = book.pages
        )
        val response = App.instance.bookApiInterface.changeBookShelfBookStatus(book.bookId, requestBody)
        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun checkAlreadyInBookShelf(book :Book) :RespondCheckInBookShelf {
        Log.d(TAG, "BookRepository: checkAlreadyInBookShelf() - called")
        if (!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookApiInterface.checkAlreadyInBookShelf(book.isbn, book.datetime)
        when(response.code()){
            200 -> {
                val respondCheckInBookShelf = response.body()
                respondCheckInBookShelf?.let { return respondCheckInBookShelf }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    companion object{
        private const val SIMPLE_SEARCH_BOOKS_ITEM_LOAD_SIZE = 6
        const val WISH_TAP_BOOKS_ITEM_LOAD_SIZE = 6
        const val READING_TAP_BOOKS_ITEM_LOAD_SIZE = 4
        const val COMPLETE_TAP_BOOKS_ITEM_LOAD_SIZE = 4
    }
}