package com.example.bookchat.ui.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.ActivitySignUpBinding
import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.ui.imagecrop.ImageCropActivity
import com.example.bookchat.ui.imagecrop.ImageCropActivity.Companion.EXTRA_CROPPED_PROFILE_BYTE_ARRAY
import com.example.bookchat.utils.PermissionManager
import com.example.bookchat.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySignUpBinding
	private val signUpViewModel: SignUpViewModel by viewModels()
	private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

	private val permissionsLauncher =
		PermissionManager.getPermissionsLauncher(this) { moveToImageCrop() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
		binding.lifecycleOwner = this
		binding.viewModel = signUpViewModel
		setFocus()
		observeUiState()
		observeUiEvent()
		initNickNameEditText()
	}

	private fun observeUiState() = lifecycleScope.launch {
		signUpViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeUiEvent() = lifecycleScope.launch {
		signUpViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun setViewState(state: SignUpState) {
		setNickNameEditTextState(state)
		setSubmitBtnState(state)
	}

	private fun setNickNameEditTextState(state: SignUpState) {
		with(binding.nickNameEt) {
			if (text.toString() != state.nickname) {
				setText(state.nickname)
				setSelection(state.nickname.length)
			}
		}
	}

	private fun setSubmitBtnState(state: SignUpState) {
		with(binding.startBookchatBtn) {
			setText(
				if (state.nicknameCheckState != NicknameCheckState.IsPerfect)
					R.string.sign_up_check_nickname_duplicate_btn
				else R.string.sign_up_submit_btn
			)
		}
	}

	private val specialCharFilter = InputFilter { source, _, _, _, _, _ ->
		val pattern = Pattern.compile(NAME_CHECK_REGULAR_EXPRESSION)
		if (pattern.matcher(source).matches().not()) {
			signUpViewModel.onEnteredSpecialChar()
			return@InputFilter ""
		}
		source
	}
	private val maxLengthFilter = InputFilter.LengthFilter(MAX_NICKNAME_LENGTH)

	private fun initNickNameEditText() {
		with(binding.nickNameEt) {
			filters = arrayOf(specialCharFilter, maxLengthFilter)
			addTextChangedListener { text: Editable? ->
				signUpViewModel.onChangeNickname(text.toString())
			}
		}
	}

	private fun setFocus() {
		binding.nickNameEt.requestFocus()
		openKeyboard(binding.nickNameEt)
	}

	private fun openKeyboard(view: View) {
		Handler(Looper.getMainLooper()).postDelayed({
			imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
		}, KEYBOARD_DELAY_TIME)
	}

	private fun closeKeyboard(windowToken: IBinder) {
		imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
	}

	private fun startUserProfileEdit() {
		closeKeyboard(binding.nickNameEt.windowToken)
		permissionsLauncher.launch(PermissionManager.getGalleryPermissions())
	}

	private fun moveToImageCrop() {
		val intent = Intent(this, ImageCropActivity::class.java)
		cropActivityResultLauncher.launch(intent)
	}

	private val cropActivityResultLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == RESULT_OK) {
				val intent = result.data
				val bitmapByteArray =
					intent?.getByteArrayExtra(EXTRA_CROPPED_PROFILE_BYTE_ARRAY)
				bitmapByteArray?.let { signUpViewModel.onChangeUserProfile(it) }
			}
		}

	private fun moveToSelectTaste(event: SignUpEvent.MoveToSelectTaste) {
		val intent = Intent(this, SelectTasteActivity::class.java)
		intent.putExtra(EXTRA_SIGNUP_USER_NICKNAME, event.userNickname)
		intent.putExtra(EXTRA_USER_PROFILE_BYTE_ARRAY, event.userProfilByteArray)
		startActivity(intent)
	}

	private fun handleEvent(event: SignUpEvent) = when (event) {
		is SignUpEvent.PermissionCheck -> startUserProfileEdit()
		is SignUpEvent.MoveToBack -> finish()
		is SignUpEvent.MoveToSelectTaste -> moveToSelectTaste(event)
		is SignUpEvent.ErrorEvent -> binding.signUpLayout.showSnackBar(event.stringId)
		is SignUpEvent.UnknownErrorEvent -> binding.signUpLayout.showSnackBar(event.message)
	}

	companion object {
		const val KEYBOARD_DELAY_TIME = 200L
		const val EXTRA_SIGNUP_USER_NICKNAME = "EXTRA_SIGNUP_USER_NICKNAME"
		const val EXTRA_USER_PROFILE_BYTE_ARRAY = "EXTRA_USER_PROFILE_BYTE_ARRAY"
		private const val NAME_CHECK_REGULAR_EXPRESSION =
			"^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55\\uFF1A]+$"
		private const val MAX_NICKNAME_LENGTH = 20
	}
}