package com.example.bookchat.ui.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.DialogReadingBookTapClickedBinding
import com.example.bookchat.ui.activity.AgonyActivity
import com.example.bookchat.ui.fragment.BookShelfFragment
import com.example.bookchat.ui.fragment.CompleteBookShelfFragment
import com.example.bookchat.utils.BookImgSizeManager
import com.example.bookchat.utils.DialogSizeManager
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.example.bookchat.viewmodel.BookShelfViewModel.Companion.COMPLETE_TAB_INDEX
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel
import com.example.bookchat.viewmodel.ReadingBookTapDialogViewModel.ReadingBookEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReadingTapBookDialog(private val bookShelfDataItem: BookShelfDataItem) : DialogFragment() {

    @Inject
    lateinit var readingBookTapDialogViewModelFactory :ReadingBookTapDialogViewModel.AssistedFactory

    private lateinit var binding : DialogReadingBookTapClickedBinding
    private val readingBookTapDialogViewModel : ReadingBookTapDialogViewModel by viewModels{
        ReadingBookTapDialogViewModel.provideFactory(readingBookTapDialogViewModelFactory, bookShelfDataItem)
    }
    private val bookShelfViewModel: BookShelfViewModel by viewModels({ getBookShelfFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.dialog_reading_book_tap_clicked,container,false)
        with(binding){
            lifecycleOwner = this@ReadingTapBookDialog
            viewmodel = readingBookTapDialogViewModel
        }
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        observeEventFlow()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setDialogSize()
        setBookImgSize()
    }

    private fun setDialogSize(){
        binding.readingDialogLayout.layoutParams.width = DialogSizeManager.dialogWidthPx
    }
    private fun setBookImgSize(){
        with(binding){
            bookImg.layoutParams.width = BookImgSizeManager.bookImgWidthPx
            bookImg.layoutParams.height = BookImgSizeManager.bookImgHeightPx
        }
    }

    private fun observeEventFlow() {
        lifecycleScope.launch{
            readingBookTapDialogViewModel.eventFlow.collect { event -> handleEvent(event) }
        }
    }

    private fun getBookShelfFragment() : BookShelfFragment {
        var fragment = requireParentFragment()
        while (fragment !is BookShelfFragment){
            fragment = fragment.requireParentFragment()
        }
        return fragment
    }

    private fun getCompleteBookTabFragment() : CompleteBookShelfFragment {
        return getBookShelfFragment().pagerAdapter.completeBookShelfFragment
    }

    private fun handleEvent(event : ReadingBookEvent) = when(event){
        is ReadingBookEvent.OpenAgonize -> { openAgonizeActivity() }

        is ReadingBookEvent.MoveToCompleteBook -> {
            val itemRemoveEvent = BookShelfViewModel.PagingViewEvent.Remove(bookShelfDataItem)
            bookShelfViewModel.addPagingViewEvent(itemRemoveEvent, ReadingStatus.READING)
            val bookShelfUiEvent = BookShelfViewModel.BookShelfEvent.ChangeBookShelfTab(COMPLETE_TAB_INDEX)
            bookShelfViewModel.startBookShelfUiEvent(bookShelfUiEvent)
            if(bookShelfViewModel.isCompleteBookLoaded){
                getCompleteBookTabFragment().completeBookShelfDataAdapter.refresh()
            }
            this.dismiss()
        }
    }

    private fun openAgonizeActivity(){
        val intent = Intent(requireContext(), AgonyActivity::class.java)
            .putExtra(EXTRA_AGONIZE_BOOK,bookShelfDataItem.bookShelfItem)
        startActivity(intent)
    }

    override fun onDestroyView() {
        readingBookTapDialogViewModel.starRating.value = 0F
        super.onDestroyView()
    }

    companion object {
        const val EXTRA_AGONIZE_BOOK = "EXTRA_AGONIZE_BOOK"
    }
}