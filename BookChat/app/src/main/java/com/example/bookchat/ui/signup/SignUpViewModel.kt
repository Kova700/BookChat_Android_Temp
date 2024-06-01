package com.example.bookchat.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookchat.R
import com.example.bookchat.data.network.model.response.ForbiddenException
import com.example.bookchat.data.network.model.response.NetworkIsNotConnectedException
import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.domain.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
	private var clientRepository: ClientRepository,
) : ViewModel() {

	private val _eventFlow = MutableSharedFlow<SignUpEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private val _uiState = MutableStateFlow<SignUpState>(SignUpState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	/** UiState변경 시 깜빡임 때문에 따로 분리 */
	private val _userProfileImage = MutableStateFlow<ByteArray>(ByteArray(0))
	val userProfileImage get() = _userProfileImage.asStateFlow()

	private fun checkNicknameDuplication(nickName: String) = viewModelScope.launch {
		runCatching { clientRepository.isDuplicatedUserNickName(nickName) }
			.onSuccess { isDuplicated ->
				updateState {
					copy(
						nicknameCheckState =
						if (isDuplicated) NicknameCheckState.IsDuplicate else NicknameCheckState.IsPerfect,
					)
				}
			}
			.onFailure { failHandler(it) }
	}

	fun onClickCameraBtn(){
		startEvent(SignUpEvent.PermissionCheck)
	}

	fun onClickStartBtn() {
		if (uiState.value.uiState == SignUpState.UiState.LOADING
			|| uiState.value.nicknameCheckState == NicknameCheckState.IsShort
			|| uiState.value.nickname.length < 2
		) return

		val nickName = uiState.value.nickname
		val userProfile = userProfileImage.value
		val nameCheckStatus = uiState.value.nicknameCheckState

		if (nameCheckStatus != NicknameCheckState.IsPerfect) {
			checkNicknameDuplication(nickName)
			return
		}

		startEvent(
			SignUpEvent.MoveToSelectTaste(
				userNickname = nickName,
				userProfilByteArray = userProfile
			)
		)
	}

	fun onChangeNickname(text: String) {
		updateUserNicknameIfValid(text.trim())
	}

	private fun updateUserNicknameIfValid(text: String) {

		if (text.length < 2) {
			updateState {
				copy(
					nickname = text,
					nicknameCheckState = NicknameCheckState.IsShort
				)
			}
			return
		}

		updateState {
			copy(
				nickname = text,
				nicknameCheckState = NicknameCheckState.Default
			)
		}
	}

	fun onEnteredSpecialChar() {
		updateState { copy(nicknameCheckState = NicknameCheckState.IsSpecialCharInText) }
	}

	fun onChangeUserProfile(profile: ByteArray) {
		_userProfileImage.value = profile
	}

	fun onClickBackBtn() {
		startEvent(SignUpEvent.MoveToBack)
	}

	fun onClickClearNickNameBtn() {
		updateState { copy(nickname = "") }
	}

	private inline fun updateState(block: SignUpState.() -> SignUpState) {
		_uiState.update { _uiState.value.block() }
	}

	private fun startEvent(event: SignUpEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private fun failHandler(exception: Throwable) {
		when (exception) {
			is ForbiddenException -> startEvent(SignUpEvent.ErrorEvent(R.string.login_forbidden_user))
			is NetworkIsNotConnectedException -> startEvent(SignUpEvent.ErrorEvent(R.string.error_network_not_connected))
			else -> {
				val errorMessage = exception.message
				if (errorMessage.isNullOrBlank()) startEvent(SignUpEvent.ErrorEvent(R.string.error_else))
				else startEvent(SignUpEvent.UnknownErrorEvent(errorMessage))
			}
		}
	}

}