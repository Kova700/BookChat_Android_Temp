package com.example.bookchat.ui.signup

import com.example.bookchat.domain.model.NicknameCheckState

data class SignUpState(
	val uiState: UiState,
	val nickname: String,
	val nicknameCheckState: NicknameCheckState,
	val clientNewImage: ByteArray?,
) {

	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
	}

	companion object {
		val DEFAULT = SignUpState(
			uiState = UiState.SUCCESS,
			nickname = "",
			nicknameCheckState = NicknameCheckState.Default,
			clientNewImage = null
		)
	}

}


sealed class SignUpEvent {
	data class MoveToSelectTaste(
		val userNickname: String,
		val userProfilByteArray: ByteArray?,
	) : SignUpEvent()

	object PermissionCheck : SignUpEvent()
	object MoveToBack : SignUpEvent()

	data class ErrorEvent(val stringId: Int) : SignUpEvent()
	data class UnknownErrorEvent(val message: String) : SignUpEvent()
}
