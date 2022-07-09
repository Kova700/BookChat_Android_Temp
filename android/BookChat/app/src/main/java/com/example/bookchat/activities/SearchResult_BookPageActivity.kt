package com.example.bookchat.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.bookchat.R
import com.example.bookchat.data.Book
import com.example.bookchat.databinding.ActivitySearchResultBookPageBinding
import com.example.bookchat.viewmodel.SearchResultBookPageViewModel

class SearchResult_BookPageActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySearchResultBookPageBinding
    private lateinit var searchResultBookPageViewModel: SearchResultBookPageViewModel
    lateinit var book :Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_result_book_page)

        with(binding){
            lifecycleOwner = this@SearchResult_BookPageActivity
            activity = this@SearchResult_BookPageActivity
            searchResultBookPageViewModel = SearchResultBookPageViewModel()
            viewModel = searchResultBookPageViewModel

        }
        book = intent.getSerializableExtra("clickedBook") as Book


    }
}