package com.example.bookchat.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.adapter.WishBookTabAdapter.Companion.BOOK_SHELF_ITEM_COMPARATOR
import com.example.bookchat.data.BookShelfDataItem
import com.example.bookchat.databinding.ItemCompleteBookTabBinding
import com.example.bookchat.utils.ReadingStatus
import com.example.bookchat.viewmodel.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CompleteBookTabAdapter(private val bookShelfViewModel: BookShelfViewModel)
    : PagingDataAdapter<BookShelfDataItem, CompleteBookTabAdapter.CompleteBookItemViewHolder>(BOOK_SHELF_ITEM_COMPARATOR){
    private lateinit var binding : ItemCompleteBookTabBinding
    private lateinit var itemClickListener : OnItemClickListener

    inner class CompleteBookItemViewHolder(val binding: ItemCompleteBookTabBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(bookShelfDataItem :BookShelfDataItem){
            with(this.binding){
                bookShelfItem = bookShelfDataItem.bookShelfItem
                setViewHolderState(swipeView, bookShelfDataItem.isSwiped)

                swipeView.setOnClickListener {
                    itemClickListener.onItemClick(bookShelfDataItem)
                }

                swipeView.setOnLongClickListener {
                    startSwipeAnimation(swipeView, bookShelfDataItem.isSwiped)
                    bookShelfDataItem.isSwiped = !bookShelfDataItem.isSwiped
                    true
                }

                swipeBackground.setOnClickListener {
                    setSwiped(false)
                    val removeWaitingEvent = BookShelfViewModel.PagingViewEvent.RemoveWaiting(bookShelfDataItem)
                    bookShelfViewModel.addPagingViewEvent(removeWaitingEvent, ReadingStatus.COMPLETE)

                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        val removeEvent = BookShelfViewModel.PagingViewEvent.Remove(bookShelfDataItem)
                        bookShelfViewModel.deleteBookShelfBookWithSwipe(bookShelfDataItem, removeEvent, ReadingStatus.COMPLETE)
                        bookShelfViewModel.removePagingViewEvent(removeWaitingEvent, ReadingStatus.COMPLETE)
                        bookShelfViewModel.addPagingViewEvent(removeEvent, ReadingStatus.COMPLETE)
                    }, SNACK_BAR_DURATION.toLong())

                    val snackCancelClickListener = View.OnClickListener {
                        bookShelfViewModel.removePagingViewEvent(removeWaitingEvent, ReadingStatus.COMPLETE)
                        handler.removeCallbacksAndMessages(null)
                    }

                    Snackbar.make(binding.root, R.string.bookshelf_delete_snack_bar, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.bookshelf_delete_snack_bar_cancel, snackCancelClickListener)
                        .setDuration(SNACK_BAR_DURATION)
                        .show()
                }
            }
        }

        fun setSwiped(flag: Boolean){
            val currentItem = getItem(absoluteAdapterPosition)
            currentItem?.let { currentItem.isSwiped = flag }
        }

        fun getSwiped(): Boolean{
            return getItem(absoluteAdapterPosition)?.isSwiped ?: false
        }

    }

    private fun setViewHolderState(view: View, isSwiped :Boolean){
        if(!isSwiped) { view.translationX = 0f; return }
        view.translationX = view.width.toFloat() * SWIPE_VIEW_PERCENT
    }

    private fun startSwipeAnimation(view: View, isSwiped :Boolean) =
        CoroutineScope(Dispatchers.Main).launch {
            val swipedX = view.width.toFloat() * SWIPE_VIEW_PERCENT
            if(isSwiped) {
                while (view.translationX > 0F){
                    view.translationX -= swipedX /20
                    delay(5L)
                }
                view.translationX = 0F
                return@launch
            }

            while (view.translationX < swipedX){
                view.translationX += swipedX /20
                delay(5L)
            }
            view.translationX = swipedX
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompleteBookItemViewHolder {
        binding = DataBindingUtil
            .inflate(LayoutInflater.from(parent.context), R.layout.item_complete_book_tab,parent,false)

        return CompleteBookItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompleteBookItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let { holder.bind(currentItem) }
    }

    interface OnItemClickListener {
        fun onItemClick(bookShelfDataItem :BookShelfDataItem)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    companion object {
        private const val SWIPE_VIEW_PERCENT = 0.3F
        private const val SNACK_BAR_DURATION = 3000
    }

}