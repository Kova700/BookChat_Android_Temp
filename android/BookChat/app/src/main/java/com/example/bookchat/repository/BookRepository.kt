package com.example.bookchat.repository

import android.util.Log
import android.widget.Toast
import com.example.bookchat.App
import com.example.bookchat.data.Book
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.SearchOptionType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookRepository {

    private lateinit var books :ArrayList<Book>
    private lateinit var call : Call<ArrayList<Book>>

    fun getBooks(searchKeyWord : String,searchOption: SearchOptionType,callback : (ArrayList<Book>) -> Unit){

        if(App.instance.networkManager.checkNetworkState()){ //네트워크 연결 체크

            when(searchOption){
                SearchOptionType.TITLE -> {call =  App.instance.apiInterface.getBookFromTitle(searchKeyWord).also {
                    Log.d(TAG, "BookRepository: getBooks() - SearchOptionType : TITLE")
                }}
                SearchOptionType.AUTHOR -> {call =  App.instance.apiInterface.getBookFromAuthor(searchKeyWord).also {
                    Log.d(TAG, "BookRepository: getBooks() - SearchOptionType : AUTHOR")
                }}
                SearchOptionType.ISBN -> {call =  App.instance.apiInterface.getBookFromIsbn(searchKeyWord).also {
                    Log.d(TAG, "BookRepository: getBooks() - SearchOptionType : ISBN")
                }}
            }

            call.enqueue(object :Callback<ArrayList<Book>>{
                override fun onResponse(
                    call: Call<ArrayList<Book>>,
                    response: Response<ArrayList<Book>>
                ) {
                    if (response.isSuccessful){
                        Log.d(TAG, "BookRepository: onResponse() - Success(통신 성공)")

                        books = response.body()!!
                        callback(books).also { //검색 결과 책들 반환
                            Log.d(TAG, "BookRepository: onResponse() - response.body() : ${response.body() }")
                        }
                    }else{
                        //응답 코드 3xx, 4xx (통신 실패)
                        Log.d(TAG, "BookRepository: onResponse() - Fail(통신 실패)")
                    }
                }

                override fun onFailure(
                    call: Call<ArrayList<Book>>,
                    t: Throwable) {
                    //인터넷 끊김 , 예외 발생 등 시스템적 이유로 통신 실패
                    Log.d(TAG, "BookRepository: onFailure() - Throwable : ${t}")
                }

            })

        }else{
            //네트워크가 연결되어있지 않음
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다. \n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
        }

    }
}