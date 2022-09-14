package com.example.bookchat.repository

import android.util.Log
import android.widget.Toast
import com.example.bookchat.App
import com.example.bookchat.data.User
import com.example.bookchat.data.UserSignUpRequestDto
import com.example.bookchat.utils.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository{
    //콜백 전부 코루틴으로 수정
    fun signUp(
        callback : (Boolean) -> Unit,
        userInfo : UserSignUpRequestDto
    ){
        if(!isNetworkConnected()) {
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val call = App.instance.apiInterface.signUp(
            nickname = userInfo.nickname,
            userEmail = userInfo.userEmail,
            oauth2Provider = userInfo.oauth2Provider,
            defaultProfileImageType = userInfo.defaultProfileImageType,
            userProfileImage = userInfo.userProfileImage!!,
            readingTastes = userInfo.readingTastes
        )
        call.enqueue(signUpCallback(callback))
    }

    fun getUserProfile(callback : (User) -> Unit){
        if(!isNetworkConnected()) {
            Toast.makeText(App.instance.applicationContext,"네트워크가 연결되어 있지 않습니다.\n네트워크를 연결해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        val call = App.instance.apiInterface.getUserProfile()
        call.enqueue(getUserProfileCallback(callback))
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.networkManager.checkNetworkState()
    }

    inner class signUpCallback(val callback : (Boolean) -> Unit) : Callback<Any> {
        override fun onResponse(call: Call<Any>, response: Response<Any>) {
            if(response.isSuccessful){
                Log.d(TAG, "signUpCallback: onResponse() - Success(통신 성공) response.body() : ${response.body()}")
                Log.d(TAG, "signUpCallback: onResponse() response.code() : ${response.code()}")
                callback(true)
                return
            }
            Log.d(TAG, "signUpCallback: onResponse() Success-fail(통신 실패) response.body() : ${response.body()}")
            Log.d(TAG, "signUpCallback: onResponse() - response.code() : ${response.code()}")
            callback(false)
        }

        override fun onFailure(call: Call<Any>, t: Throwable) {
            Log.d(TAG, "signUpCallback: onFailure() - Fail(통신 실패) response.body() : ${t.message}")
            callback(false)
        }

    }

    inner class getUserProfileCallback(val callback : (User) -> Unit) : Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
            if(response.isSuccessful){
                Log.d(TAG, "UesrRepository: onResponse() - Success(통신 성공) response.body() : ${response.body()}")
                Log.d(TAG, "getUserProfileCallback: onResponse() - called")
                val user = response.body()!!
                callback(user)
                return
            }
            Log.d(TAG, "UesrRepository: onResponse() - Fail(통신 실패) 응답 코드: ${response.code()}")
            Toast.makeText(App.instance.applicationContext,"서버와의 연결을 실패했습니다.\n응답 코드: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
        override fun onFailure(call: Call<User>, t: Throwable) {
            //인터넷 끊김 , 예외 발생 등 시스템적 이유로 통신 실패
            Log.d(TAG, "UesrRepository: onFailure()- Throwable : ${t} ")
        }
    }

}

