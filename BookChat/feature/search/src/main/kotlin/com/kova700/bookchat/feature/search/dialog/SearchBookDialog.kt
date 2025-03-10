package com.kova700.bookchat.feature.search.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kova700.bookchat.core.data.search.channel.external.model.SearchFilter
import com.kova700.bookchat.feature.search.SearchFragment
import com.kova700.bookchat.feature.search.databinding.DialogSearchBookClickedBinding
import com.kova700.bookchat.feature.search.model.SearchPurpose
import com.kova700.bookchat.feature.search.model.SearchTarget
import com.kova700.bookchat.feature.search.searchdetail.SearchDetailActivity
import com.kova700.bookchat.util.book.BookImgSizeManager
import com.kova700.bookchat.util.dialog.DialogSizeManager
import com.kova700.bookchat.util.image.image.loadUrl
import com.kova700.bookchat.util.snackbar.showSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchBookDialog : DialogFragment() {

	private var _binding: DialogSearchBookClickedBinding? = null
	private val binding get() = _binding!!

	private val searchBookDialogViewModel: SearchBookDialogViewModel by viewModels()

	@Inject
	lateinit var bookImgSizeManager: BookImgSizeManager

	@Inject
	lateinit var dialogSizeManager: DialogSizeManager

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DialogSearchBookClickedBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		observeUiState()
		observeEvent()
		initViewState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		searchBookDialogViewModel.uiState.collect { state ->
			setViewState(state)
		}
	}

	private fun observeEvent() = viewLifecycleOwner.lifecycleScope.launch {
		searchBookDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
	}

	private fun initViewState() {
		bookImgSizeManager.setBookImgSize(binding.bookImg)
		dialogSizeManager.setDialogSize(binding.dialogLayout)
		with(binding) {
			channelSearchBtn.setOnClickListener { searchBookDialogViewModel.onClickChatBtn() }
			heartBtn.setOnClickListener { searchBookDialogViewModel.onClickWishToggleBtn() }
			changeStatusToCompleteBtn.setOnClickListener { searchBookDialogViewModel.onClickCompleteBtn() }
			changeStatusToReadingBtn.setOnClickListener { searchBookDialogViewModel.onClickReadingBtn() }
		}
	}

	private fun setViewState(uiState: SearchDialogUiState) {
		with(binding) {
			bookImg.loadUrl(uiState.book.bookCoverImageUrl)
			bookTitleTv.isSelected = true
			bookTitleTv.text = uiState.book.title
			bookAuthorsTv.isSelected = true
			bookAuthorsTv.text = uiState.book.authorsString
			bookPublishATv.isSelected = true
			bookPublishATv.text = uiState.book.publishAt
		}
		setViewVisibility(uiState)
	}

	private fun setViewVisibility(uiState: SearchDialogUiState) {
		with(binding) {
			loadingProgressbar.visibility =
				if (uiState.isLoading) View.VISIBLE else View.INVISIBLE
			alreadyInBookshelfBtn.visibility =
				if (uiState.isAlreadyInBookShelf) View.VISIBLE else View.INVISIBLE
			resultRetryLayout.root.visibility =
				if (uiState.isInitError) View.VISIBLE else View.INVISIBLE
			successStateGroup.visibility =
				if (uiState.isSuccess) View.VISIBLE else View.INVISIBLE
			changeStatusToCompleteBtn.visibility =
				if (uiState.isNotInBookShelf) View.VISIBLE else View.INVISIBLE
			changeStatusToReadingBtn.visibility =
				if (uiState.isNotInBookShelf) View.VISIBLE else View.INVISIBLE
			heartBtn.visibility =
				if (uiState.isNotInBookShelf) View.VISIBLE else View.INVISIBLE
			heartBtn.isChecked = uiState.isAlreadyInWishBookShelf
		}
	}

	private fun moveToChannelSearchWithSelectedBook(
		searchKeyword: String,
		searchTarget: SearchTarget,
		searchPurpose: SearchPurpose,
		searchFilter: SearchFilter,
	) {
		val intent = Intent(requireActivity(), SearchDetailActivity::class.java)
		intent.putExtra(SearchFragment.EXTRA_SEARCH_KEYWORD, searchKeyword)
		intent.putExtra(SearchFragment.EXTRA_SEARCH_PURPOSE, searchPurpose)
		intent.putExtra(SearchFragment.EXTRA_SEARCH_TARGET, searchTarget)
		intent.putExtra(SearchFragment.EXTRA_SEARCH_FILTER, searchFilter)
		startActivity(intent)
	}

	private fun moveToStarSetDialog() {
		val dialog = CompleteBookStarSetDialog()
		dialog.show(childFragmentManager, DIALOG_TAG_STAR_SET)
	}

	private fun handleEvent(event: SearchTapDialogEvent) = when (event) {
		is SearchTapDialogEvent.MoveToStarSetDialog -> moveToStarSetDialog()
		is SearchTapDialogEvent.ShowSnackBar -> binding.root.showSnackBar(
			textId = event.stringId,
			anchor = binding.changeStatusToReadingBtn
		)

		is SearchTapDialogEvent.MoveToChannelSearchWithSelectedBook -> moveToChannelSearchWithSelectedBook(
			searchKeyword = event.searchKeyword,
			searchTarget = event.searchTarget,
			searchPurpose = event.searchPurpose,
			searchFilter = event.searchFilter
		)
	}

	companion object {
		const val DIALOG_TAG_STAR_SET = "DIALOG_TAG_STAR_SET"
	}

}