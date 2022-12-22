package com.example.bookchat.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.data.UserSignUpDto
import com.example.bookchat.repository.UserRepository
import com.example.bookchat.response.ForbiddenException
import com.example.bookchat.response.NetworkIsNotConnectedException
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.ReadingTaste
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectTasteViewModel @Inject constructor(
    private val userRepository : UserRepository
    ) :ViewModel() {

    private val _eventFlow = MutableSharedFlow<SelectTasteEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val selectedTastes = ArrayList<ReadingTaste>()

    private val _isTastesEmpty = MutableStateFlow<Boolean>(true)
    val isTastesEmpty = _isTastesEmpty.asStateFlow()

    val _signUpDto = MutableStateFlow<UserSignUpDto>(UserSignUpDto())

    fun signUp() = viewModelScope.launch {
        _signUpDto.value.readingTastes = selectedTastes
        runCatching{ userRepository.signUp(_signUpDto.value) }
            .onSuccess { signIn() }
            .onFailure { failHandler(it) }
    }

    private fun signIn() = viewModelScope.launch {
        Log.d(TAG, "SelectTasteViewModel: signIn() - called")
        runCatching { userRepository.signIn() }
            .onSuccess { requestUserInfo() }
            .onFailure { failHandler(it) }
    }

    private fun requestUserInfo() = viewModelScope.launch {
        Log.d(TAG, "LoginViewModel: requestUserInfo() - called")
        runCatching{ userRepository.getUserProfile() }
            .onSuccess { startEvent(SelectTasteEvent.MoveToMain) }
            .onFailure { failHandler(it) }
    }

    fun clickTaste(pickedReadingTaste: ReadingTaste) {
        if (selectedTastes.contains(pickedReadingTaste)) selectedTastes.remove(pickedReadingTaste)
        else selectedTastes.add(pickedReadingTaste)
        emptyCheck()
        Log.d(TAG, "SelectTasteViewModel: clickTaste() - selectedTastes : $selectedTastes")
    }

    private fun emptyCheck() {
        if (selectedTastes.isEmpty()){
            _isTastesEmpty.value = true
            return
        }
        _isTastesEmpty.value = false
    }
    fun clickBackBtn() {
        startEvent(SelectTasteEvent.MoveToBack)
    }

    private fun startEvent (event : SelectTasteEvent) = viewModelScope.launch {
        _eventFlow.emit(event)
    }

    sealed class SelectTasteEvent {
        object MoveToMain :SelectTasteEvent()
        object Forbidden : SelectTasteEvent()
        object NetworkError : SelectTasteEvent()
        object UnknownError : SelectTasteEvent()
        object MoveToBack : SelectTasteEvent()
    }

    private fun failHandler(exception: Throwable) {
        Log.d(TAG, "SelectTasteViewModel: failHandler() - called")
        when(exception){
            is ForbiddenException -> startEvent(SelectTasteEvent.Forbidden)
            is NetworkIsNotConnectedException -> startEvent(SelectTasteEvent.NetworkError)
            else -> startEvent(SelectTasteEvent.UnknownError)
        }
    }
}