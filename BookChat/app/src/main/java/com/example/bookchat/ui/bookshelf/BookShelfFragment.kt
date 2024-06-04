package com.example.bookchat.ui.bookshelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentBookshelfBinding
import com.example.bookchat.ui.bookshelf.complete.CompleteBookShelfFragment
import com.example.bookchat.ui.bookshelf.reading.ReadingBookShelfFragment
import com.example.bookchat.ui.bookshelf.wish.WishBookBookShelfFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookShelfFragment : Fragment() {

	private var _binding: FragmentBookshelfBinding? = null
	private val binding get() = _binding!!
	private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

	private val fragments: List<Fragment> =
		listOf(WishBookBookShelfFragment(), ReadingBookShelfFragment(), CompleteBookShelfFragment())

	private lateinit var viewPagerAdapter: ViewPagerAdapter

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bookshelf, container, false)
		viewPagerAdapter = ViewPagerAdapter(fragments, this)
		binding.lifecycleOwner = viewLifecycleOwner
		binding.viewPager.adapter = viewPagerAdapter
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initTapLayout()
		inflateFirstTab(1)
		observeEvent()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun inflateFirstTab(tabIndex: Int) {
		binding.viewPager.setCurrentItem(tabIndex, false)
	}

	private fun observeEvent() {
		lifecycleScope.launch {
			bookShelfViewModel.eventFlow.collect { event -> handleEvent(event) }
		}
	}

	private fun initTapLayout() {
		TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
			tab.text = resources.getString(bookShelfTapNameList[position])
		}.attach()
	}

	private fun changeTab(tabIndex: Int) {
		binding.viewPager.currentItem = tabIndex
	}

	private fun handleEvent(event: BookShelfEvent) = when (event) {
		is BookShelfEvent.ChangeBookShelfTab -> {
			changeTab(event.tapIndex)
		}
	}

	companion object {
		private val bookShelfTapNameList =
			listOf(R.string.wish_book, R.string.reading_book, R.string.complete_book)
	}
}