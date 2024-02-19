package com.example.bookchat.repository

import com.example.bookchat.App
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.data.request.RequestUserSignIn
import com.example.bookchat.data.request.RequestUserSignUp
import com.example.bookchat.data.response.NeedToDeviceWarningException
import com.example.bookchat.data.response.NeedToSignUpException
import com.example.bookchat.data.response.NetworkIsNotConnectedException
import com.example.bookchat.data.response.NickNameDuplicateException
import com.example.bookchat.data.response.ResponseBodyEmptyException
import com.example.bookchat.utils.DataStoreManager
import javax.inject.Inject

class UserRepository @Inject constructor(){

    suspend fun signIn(approveChangingDevice: Boolean = false) {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val idToken = DataStoreManager.getIdToken()
        val fcmToken = DataStoreManager.getFCMToken()
        val deviceID = DataStoreManager.getDeviceID()
        val requestUserSignIn = RequestUserSignIn(
            fcmToken, deviceID, approveChangingDevice, idToken.oAuth2Provider
        )

        val response = App.instance.bookChatApiClient.signIn(idToken.token, requestUserSignIn)
        when(response.code()){
            200 -> {
                val token = response.body()
                token?.let { DataStoreManager.saveBookChatToken(token); return }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            404 ->  throw NeedToSignUpException(response.errorBody()?.string())
            409 -> throw NeedToDeviceWarningException(response.errorBody()?.string())
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun signUp(userSignUpDto : UserSignUpDto) {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val idToken = DataStoreManager.getIdToken()
        val requestUserSignUp = RequestUserSignUp(
            oauth2Provider = idToken.oAuth2Provider,
            nickname = userSignUpDto.nickname,
            readingTastes = userSignUpDto.readingTastes
        )

        val response = App.instance.bookChatApiClient.signUp(
            idToken = idToken.token,
            userProfileImage = userSignUpDto.userProfileImage,
            requestUserSignUp = requestUserSignUp
        )

        when(response.code()){
            200 -> { }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun signOut(){
        DataStoreManager.deleteBookChatToken()
    }

    //회원 탈퇴 후 재가입 가능 기간 정책 결정해야함
    suspend fun withdraw() {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()
        val response = App.instance.bookChatApiClient.withdraw()
        when(response.code()){
            200 -> signOut()
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun getUserProfile(){
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.getUserProfile()
        when(response.code()){
            200 -> {
                val user = response.body()
                user?.let { App.instance.cacheUser(user); return }
                throw ResponseBodyEmptyException(response.errorBody()?.string())
            }
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    suspend fun requestNameDuplicateCheck(nickName : String) {
        if(!isNetworkConnected()) throw NetworkIsNotConnectedException()

        val response = App.instance.bookChatApiClient.requestNameDuplicateCheck(nickName)
        when(response.code()){
            200 -> { }
            409 -> throw NickNameDuplicateException(response.errorBody()?.string())
            else -> throw Exception(createExceptionMessage(response.code(),response.errorBody()?.string()))
        }
    }

    private fun createExceptionMessage(responseCode :Int, responseErrorBody :String?) :String {
        return "responseCode : $responseCode , responseErrorBody : $responseErrorBody"
    }

    private fun isNetworkConnected() :Boolean{
        return App.instance.isNetworkConnected()
    }
}

