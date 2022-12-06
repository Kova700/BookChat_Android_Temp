package com.example.bookchat.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.data.RequestRegisterBookShelfBook
import com.example.bookchat.repository.BookRepository
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingStatus
import kotlinx.coroutines.launch

class SearchTapBookDialogViewModel(private val bookRepository: BookRepository) : ViewModel() {
    lateinit var book: Book

    fun requestRegisterWishBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterWishBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book, ReadingStatus.WISH)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Wish등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Wish등록 실패",Toast.LENGTH_SHORT).show()
            }
    }

    fun requestRegisterReadingBook() = viewModelScope.launch {
        Log.d(TAG, "SearchTapBookDialogViewModel: requestRegisterReadingBook() - called")
        val requestRegisterBookShelfBook = RequestRegisterBookShelfBook(book,ReadingStatus.READING)
        runCatching { bookRepository.registerBookShelfBook(requestRegisterBookShelfBook) }
            .onSuccess {
                Toast.makeText(App.instance.applicationContext,"Reading등록 성공",Toast.LENGTH_SHORT).show()
            }
            .onFailure {
                Toast.makeText(App.instance.applicationContext,"Reading등록 실패",Toast.LENGTH_SHORT).show()
            }

    }

}