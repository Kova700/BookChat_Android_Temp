package com.kova700.bookchat.feature.mypage.setting.accountsetting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kova700.bookchat.core.chatclient.ChatClient
import com.kova700.bookchat.core.design_system.R
import com.kova700.bookchat.feature.mypage.setting.accountsetting.AccountSettingUiState.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingViewModel @Inject constructor(
	private val chatClient: ChatClient,
) : ViewModel() {

	private val _uiState = MutableStateFlow<AccountSettingUiState>(AccountSettingUiState.DEFAULT)
	val uiState = _uiState.asStateFlow()

	private val _eventFlow = MutableSharedFlow<AccountSettingUiEvent>()
	val eventFlow = _eventFlow.asSharedFlow()

	private fun logout() = viewModelScope.launch {
		if (_uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chatClient.logout() }
			.onSuccess { startEvent(AccountSettingUiEvent.StartOAuthLogout) }
			.onFailure {
				startEvent(AccountSettingUiEvent.ShowSnackBar(R.string.sign_out_fail))
				updateState { copy(uiState = UiState.ERROR) }
			}
	}

	private fun withdraw() = viewModelScope.launch {
		if (_uiState.value.isLoading) return@launch
		updateState { copy(uiState = UiState.LOADING) }
		runCatching { chatClient.withdraw() }
			.onSuccess { startEvent(AccountSettingUiEvent.StartOAuthWithdraw) }
			.onFailure {
				startEvent(AccountSettingUiEvent.ShowSnackBar(R.string.withdraw_fail))
				updateState { copy(uiState = UiState.ERROR) }
			}
	}

	fun onSuccessOAuthLogout() {
		startEvent(AccountSettingUiEvent.MoveToLoginPage)
	}

	fun onSuccessOAuthWithdraw() {
		startEvent(AccountSettingUiEvent.MoveToLoginPage)
	}

	fun onClickLogoutBtn() {
		logout()
	}

	fun onClickWithdrawConfirm() {
		withdraw()
	}

	fun onClickWithdrawBtn() {
		startEvent(AccountSettingUiEvent.ShowWithdrawWarningDialog)
	}

	fun onClickBackBtn() {
		if (_uiState.value.isLoading) return
		startEvent(AccountSettingUiEvent.MoveToBack)
	}

	private fun startEvent(event: AccountSettingUiEvent) = viewModelScope.launch {
		_eventFlow.emit(event)
	}

	private inline fun updateState(block: AccountSettingUiState.() -> AccountSettingUiState) {
		_uiState.update { _uiState.value.block() }
	}
}