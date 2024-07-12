package com.example.bookchat.ui

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.drawerlayout.widget.DrawerLayout
import com.example.bookchat.R
import com.example.bookchat.data.*
import com.example.bookchat.domain.model.AgonyFolderHexColor
import com.example.bookchat.domain.model.NicknameCheckState
import com.example.bookchat.domain.model.UserDefaultProfileType
import com.example.bookchat.ui.agony.agonyrecord.model.AgonyRecordListItem
import com.example.bookchat.ui.login.LoginUiState
import com.example.bookchat.utils.*
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.image.loadBitmap
import com.example.bookchat.utils.image.loadByteArray
import com.example.bookchat.utils.image.loadUrl
import com.example.bookchat.utils.image.loadUserProfile
import java.util.*

object DataBindingAdapter {

	/**텍스트뷰 select 설정*/
	@JvmStatic
	@BindingAdapter("setSelected")
	fun setSelected(view: View, boolean: Boolean) {
		view.isSelected = boolean
	}

	/**이미지뷰 이미지 설정(URL)*/
	@JvmStatic
	@BindingAdapter("loadUrl")
	fun loadUrl(imageView: ImageView, url: String?) {
		imageView.loadUrl(url)
	}

	/**이미지뷰 이미지 설정(Bitmap)*/
	@JvmStatic
	@BindingAdapter("loadBitmap")
	fun loadBitmap(imageView: ImageView, bitmap: Bitmap?) {
		imageView.loadBitmap(bitmap)
	}

	/**이미지뷰 이미지 설정(ByteArray)*/
	@JvmStatic
	@BindingAdapter("loadByteArray")
	fun loadByteArray(imageView: ImageView, byteArray: ByteArray?) {
		imageView.loadByteArray(byteArray)
	}

	/**유저 프로필 이미지 출력*/
	@JvmStatic
	@BindingAdapter("userProfileUrl", "userDefaultProfileImageType", requireAll = false)
	fun loadUserProfile(
		imageView: ImageView,
		userProfileUrl: String?,
		userDefaultProfileType: UserDefaultProfileType?,
	) {
		imageView.loadUserProfile(userProfileUrl, userDefaultProfileType)
	}

	@JvmStatic
	@BindingAdapter("setUserNickname")
	fun setUserNickname(textview: TextView, nickname: String?) {
		if (nickname.isNullOrBlank()) {
			textview.text = "(알 수 없음)"
			return
		}
		textview.text = nickname
	}

	/**독서취향 : 제출 버튼 색상 설정*/
	@JvmStatic
	@BindingAdapter("setButtonColor")
	fun setButtonColor(button: Button, booleanFlag: Boolean) {
		if (booleanFlag) {
			button.setBackgroundColor(Color.parseColor("#D9D9D9"))
			button.isEnabled = false
			return
		}
		button.setBackgroundColor(Color.parseColor("#5648FF"))
		button.isEnabled = true
	}

	/**회원가입 :닉네임 검사 상황별 안내문구*/
	//텍스트 (색 , 글자)
	@JvmStatic
	@BindingAdapter("setTextViewFromCheckResult")
	fun setTextViewFromCheckResult(textView: TextView, nicknameCheckState: NicknameCheckState) {
		when (nicknameCheckState) {
			NicknameCheckState.Default -> textView.text = ""

			NicknameCheckState.IsShort -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_short)
			}

			NicknameCheckState.IsDuplicate -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_duplicate)
			}

			NicknameCheckState.IsSpecialCharInText -> {
				textView.setTextColor(Color.parseColor("#FF004D"))
				textView.text =
					textView.context.resources.getString(R.string.name_check_status_special_char)
			}

			NicknameCheckState.IsPerfect -> {
				textView.setTextColor(Color.parseColor("#5648FF"))
				textView.text = textView.context.resources.getString(R.string.name_check_status_perfect)
			}

		}
	}

	/**회원가입 :닉네임 검사 상황별 테두리 색상*/
	//레이아웃 (테두리)
	@JvmStatic
	@BindingAdapter("setLayoutFromCheckResult")
	fun setLayoutFromCheckResult(view: View, nicknameCheckState: NicknameCheckState) {
		Log.d(
			TAG,
			"DataBindingAdapter: setLayoutFromCheckResult() - nicknameCheckState : $nicknameCheckState"
		)
		when (nicknameCheckState) {
			NicknameCheckState.Default,
			-> {
				view.background = ResourcesCompat.getDrawable(
					view.resources,
					R.drawable.nickname_input_back_white,
					null
				)
			}

			NicknameCheckState.IsShort,
			NicknameCheckState.IsDuplicate,
			NicknameCheckState.IsSpecialCharInText,
			-> {
				view.background = ResourcesCompat.getDrawable(
					view.resources,
					R.drawable.nickname_input_back_red,
					null
				)
			}

			NicknameCheckState.IsPerfect -> {
				view.background = ResourcesCompat.getDrawable(
					view.context.resources,
					R.drawable.nickname_input_back_blue,
					null
				)
			}
		}
	}

	/**고민 폴더 색상 설정(HexColor)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderBackgroundTint")
	fun setAgonyFolderBackgroundTint(
		view: View,
		agonyFolderHexColor: AgonyFolderHexColor,
	) {
		view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(agonyFolderHexColor.hexcolor))
	}

	/**고민 폴더 text 색상 설정(HexColor, ItemStatus)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderTextColor")
	fun setAgonyFolderTextColor(
		textView: TextView,
		agonyFolderHexColor: AgonyFolderHexColor,
	) {
		when (agonyFolderHexColor) {
			AgonyFolderHexColor.WHITE,
			AgonyFolderHexColor.YELLOW,
			AgonyFolderHexColor.ORANGE,
			-> {
				textView.setTextColor(Color.parseColor("#595959"))
			}

			AgonyFolderHexColor.BLACK,
			AgonyFolderHexColor.GREEN,
			AgonyFolderHexColor.PURPLE,
			AgonyFolderHexColor.MINT,
			-> {
				textView.setTextColor(Color.parseColor("#FFFFFF"))
			}
		}
	}

	/**고민 폴더 Editting Mode 색상 설정(HexColor, ItemStatus)*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderEdittingBackgroundTint")
	fun setAgonyFolderEdittingBackgroundTint(
		view: View,
		isSelected: Boolean,
	) {
		when (isSelected) {
			false -> {
				view.backgroundTintList =
					ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.agony_color_white))
			}

			true -> {
				view.backgroundTintList =
					ColorStateList.valueOf(ContextCompat.getColor(view.context, R.color.agony_color_selected))
			}
		}
	}

	/**고민 폴더 체크 여부 Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setAgonyFolderEditingCheckedVisibility")
	fun setAgonyFolderEditingCheckedVisibility(
		view: View,
		isSelected: Boolean,
	) {
		when (isSelected) {
			false -> view.visibility = View.INVISIBLE
			true -> view.visibility = View.VISIBLE
		}
	}

	/**고민 생성 Dialog text 색상 설정*/
	@JvmStatic
	@BindingAdapter("setMakeAgonyTextColorWithFolderHexColor")
	fun setMakeAgonyTextColorWithFolderHexColor(
		textView: TextView,
		agonyFolderHexColor: AgonyFolderHexColor,
	) {
		when (agonyFolderHexColor) {
			AgonyFolderHexColor.WHITE,
			AgonyFolderHexColor.YELLOW,
			AgonyFolderHexColor.ORANGE,
			-> {
				textView.setTextColor(Color.parseColor("#595959"))
				textView.setHintTextColor(Color.parseColor("#595959"))
			}

			AgonyFolderHexColor.BLACK,
			AgonyFolderHexColor.GREEN,
			AgonyFolderHexColor.PURPLE,
			AgonyFolderHexColor.MINT,
			-> {
				textView.setTextColor(Color.parseColor("#FFFFFF"))
				textView.setHintTextColor(Color.parseColor("#FFFFFF"))
			}
		}
	}

	/**고민 Color Circle Click 가능 여부 설정*/
	@JvmStatic
	@BindingAdapter("setClickableWithAgonyFolderHexColor")
	fun setClickableWithAgonyFolderHexColor(toggleButton: ToggleButton, checkedFlag: Boolean) {
		toggleButton.isClickable = !checkedFlag
	}

	/**고민 기록 FirstItem Default State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityInFirstItemView")
	fun setVisibilityInFirstItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState is AgonyRecordListItem.ItemState.Success) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityInEditingItemView")
	fun setVisibilityInEditingItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState is AgonyRecordListItem.ItemState.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 FirstItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityInLoadingItemView")
	fun setVisibilityInLoadingItemView(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState == AgonyRecordListItem.ItemState.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Default State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInDefaultState")
	fun setVisibilityDataItemInDefaultState(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState is AgonyRecordListItem.ItemState.Success) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Editing State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInEditingState")
	fun setVisibilityDataItemInEditingState(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState is AgonyRecordListItem.ItemState.Editing) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**고민 기록 DataItem Loading State일 때 View Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityDataItemInLoadingState")
	fun setVisibilityDataItemInLoadingState(
		view: View,
		itemState: AgonyRecordListItem.ItemState,
	) {
		if (itemState == AgonyRecordListItem.ItemState.Loading) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**Login 페이지 Loading UI Visibility 설정*/
	@JvmStatic
	@BindingAdapter("setVisibilityLoadingUIInLogin")
	fun setVisibilityLoadingUIInLogin(view: View, uiState: LoginUiState.UiState) {
		if (uiState == LoginUiState.UiState.LOADING) {
			view.visibility = View.VISIBLE
			return
		}
		view.visibility = View.INVISIBLE
	}

	/**UserChatRoomListItem 시간 Text 세팅*/
	@JvmStatic
	@BindingAdapter("getFormattedDetailDateTimeText")
	fun getFormattedDetailDateTimeText(view: TextView, dateAndTimeString: String?) {
		if (dateAndTimeString.isNullOrBlank()) return
		view.text = DateManager.getFormattedDetailDateTimeText(dateAndTimeString)
	}

	/**Shimmer Animation Start/Stop 설정*/
	@JvmStatic
	@BindingAdapter("setChatInputEtFocusChangeListener")
	fun setChatInputEtFocusChangeListener(editText: EditText, bool: Boolean) {
		editText.setOnFocusChangeListener { view, hasFocus ->
			if (view !is EditText) return@setOnFocusChangeListener

			if (hasFocus) {
				view.maxLines = 4
				return@setOnFocusChangeListener
			}
			view.maxLines = 1
		}
	}

	/**DrawerLayout 스와이프 off 세팅*/
	@JvmStatic
	@BindingAdapter("setDrawerLayoutSwipeOff")
	fun setDrawerLayoutSwipeOff(view: DrawerLayout, bool: Boolean?) {
		view.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
	}
}