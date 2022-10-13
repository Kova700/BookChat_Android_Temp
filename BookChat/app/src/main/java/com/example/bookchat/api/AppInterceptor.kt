package com.example.bookchat.api

import android.util.Log
import com.example.bookchat.utils.Constants.TAG
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AppInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(
        chain: Interceptor.Chain // chain : 가로채지기 직전의 요청에 대한 정보가 모두 들어있음
    ): Response{
        Log.d(TAG, "AppInterceptor: intercept() - chain.request() : ${chain.request()}")
        return with(chain) {
            val tokenAddedRequest = request().newBuilder() //앞에 요청 내용 모두 복사 (리퀘스트는 불변객체이기 때문)
//                .addHeader()
                .build() //생성
            proceed(tokenAddedRequest) //서버로부터 새로운 Request 정보로 요청을 보낸 뒤 받은 응답 값
        } //체인에 추가
    }
}