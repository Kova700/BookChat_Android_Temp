package com.example.bookchat.viewmodel

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.response.NickNameDuplicateException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.NameCheckStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

class SignUpViewModel(private var userRepository : UserRepository) :ViewModel() {
    private val _eventFlow = MutableSharedFlow<SignUpEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var isNotNameShort = false
    private var isNotNameDuplicate = false
    val _signUpDto = MutableStateFlow<UserSignUpDto>(UserSignUpDto( defaultProfileImageType = Random().nextInt(5) + 1) )

    private val _nameCheckStatus = MutableStateFlow<NameCheckStatus>(NameCheckStatus.Default)
    val nameCheckStatus = _nameCheckStatus.asStateFlow()

    val _userProfilByteArray = MutableStateFlow<ByteArray>(byteArrayOf())
    val userProfilByteArray = _userProfilByteArray.asStateFlow()

    val editTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            renewNameCheckStatus(s.toString())
            isNotNameDuplicate = false
            Log.d(TAG, "SignUpViewModel: afterTextChanged() - _signUpDto : ${_signUpDto.value}")
        }
    }

    fun renewNameCheckStatus(inputedText :String){
        if (inputedText.length < 2){
            _nameCheckStatus.value = NameCheckStatus.IsShort
            isNotNameShort = false
            return
        }
        _nameCheckStatus.value = NameCheckStatus.Default
        isNotNameShort = true
    }

    val specialCharFilter = arrayOf(InputFilter{ source, _, _, _, _, _ ->
        val regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
        val pattern = Pattern.compile(regex)
        if (pattern.matcher(source).matches()) return@InputFilter source
        _nameCheckStatus.value = NameCheckStatus.IsSpecialCharInText
        return@InputFilter ""
    })

    fun clickStartBtn() = viewModelScope.launch {
        if (isNotNameShort){
            if (isNotNameDuplicate){
                startEvent(SignUpEvent.MoveToSelectTaste(_signUpDto.value,_userProfilByteArray.value))
                return@launch
            }
            requestNameDuplicateCheck(_signUpDto.value.nickname)
            return@launch
        }
        renewNameCheckStatus("")
    }

    private suspend fun requestNameDuplicateCheck(nickName :String) {
        Log.d(TAG, "SignUpViewModel: requestNameDuplicateCheck() - called")
        runCatching { userRepository.requestNameDuplicateCheck(nickName) }
            .onSuccess { _nameCheckStatus.value = NameCheckStatus.IsPerfect; isNotNameDuplicate = true }
            .onFailure { failHandler(it) }
    }

    fun openGallery(){
        startEvent(SignUpEvent.PermissionCheck)
    }
    fun clickBackBtn() {
        startEvent(SignUpEvent.MoveToBack)
    }

    private fun startEvent (event : SignUpEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class SignUpEvent {
        data class MoveToSelectTaste(val signUpDto :UserSignUpDto, val userProfilByteArray :ByteArray) :SignUpEvent()
        object PermissionCheck :SignUpEvent()
        object MoveToBack :SignUpEvent()
        object UnknownError :SignUpEvent()
    }

    private fun failHandler(exception: Throwable) {
        Log.d(TAG, "SignUpViewModel: failHandler() - called")
        when(exception){
            is NickNameDuplicateException -> { _nameCheckStatus.value = NameCheckStatus.IsDuplicate; isNotNameDuplicate = false }
            else -> startEvent(SignUpEvent.UnknownError)
        }
    }
}