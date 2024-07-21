package com.example.bookchat.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.R
import com.example.bookchat.databinding.FragmentHomeBinding
import com.example.bookchat.domain.model.Channel
import com.example.bookchat.ui.MainActivity
import com.example.bookchat.ui.channel.chatting.ChannelActivity
import com.example.bookchat.ui.channelList.ChannelListFragment
import com.example.bookchat.ui.createchannel.MakeChannelActivity
import com.example.bookchat.ui.home.book.adapter.HomeBookAdapter
import com.example.bookchat.ui.home.book.adapter.HomeBookItemDecoration
import com.example.bookchat.ui.home.book.model.HomeBookItem
import com.example.bookchat.ui.home.channel.adapter.HomeChannelAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO : 독서중 도서 API 요청 후 로컬 DB 저장 (API 스펙에 BOOKID가 추가되어야함)
@AndroidEntryPoint
class HomeFragment : Fragment() {

	private var _binding: FragmentHomeBinding? = null
	private val binding get() = _binding!!
	private val homeViewModel: HomeViewModel by viewModels()

	@Inject
	lateinit var mainReadingBookAdapter: HomeBookAdapter

	@Inject
	lateinit var homeChannelAdapter: HomeChannelAdapter

	@Inject
	lateinit var homeBookItemDecoration: HomeBookItemDecoration

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		_binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
		binding.lifecycleOwner = this.viewLifecycleOwner
		binding.viewmodel = homeViewModel
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initAdapter()
		initRecyclerView()
		initViewState()
		observeUiEvent()
		observeUiState()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun observeUiEvent() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.eventFlow.collect(::handleEvent)
	}

	private fun observeUiState() = viewLifecycleOwner.lifecycleScope.launch {
		homeViewModel.uiState.collect { uiState ->
			mainReadingBookAdapter.submitList(uiState.readingBookShelfBooks)
			homeChannelAdapter.submitList(uiState.channels)
			setEmptyUiVisibility(uiState.readingBookShelfBooks, uiState.channels)
		}
	}

	private fun initViewState() {
		binding.bookAddBtn.setOnClickListener {
			(requireActivity() as MainActivity).navigateToSearchFragment()
		}
		binding.chatRoomAddBtn.setOnClickListener { moveToMakeChannel() }
	}

	private fun setEmptyUiVisibility(bookItems: List<HomeBookItem>, channels: List<Channel>) {
		binding.emptyReadingBookLayout.visibility =
			if (bookItems.isEmpty()) View.VISIBLE else View.INVISIBLE
		binding.emptyChatRoomLayout.visibility =
			if (channels.isEmpty()) View.VISIBLE else View.INVISIBLE
	}

	private fun initAdapter() {
		initBookAdapter()
		initChatRoomAdapter()
	}

	private fun initRecyclerView() {
		initBookRcv()
		initChatRoomRcv()
	}

	private fun initBookAdapter() {
		mainReadingBookAdapter.onItemClick = { itemPosition ->
			homeViewModel.onBookItemClick(mainReadingBookAdapter.currentList[itemPosition].bookShelfId)
		}
	}

	private fun initChatRoomAdapter() {
		homeChannelAdapter.onItemClick = { itemPosition ->
			homeViewModel.onChannelItemClick(homeChannelAdapter.currentList[itemPosition].roomId)
		}
	}

	private fun initBookRcv() {
		with(binding.readingBookRcvMain) {
			adapter = mainReadingBookAdapter
			addItemDecoration(homeBookItemDecoration)
			layoutManager =
				LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
		}
	}

	private fun initChatRoomRcv() {
		with(binding.chatRoomUserInRcv) {
			adapter = homeChannelAdapter
			layoutManager = LinearLayoutManager(requireContext())
		}
	}

	private fun moveToReadingBookShelf() {
		(requireActivity() as MainActivity).navigateToBookShelfFragment()
	}

	private fun moveToChannel(channelId: Long) {
		val intent = Intent(requireContext(), ChannelActivity::class.java)
		intent.putExtra(ChannelListFragment.EXTRA_CHANNEL_ID, channelId)
		startActivity(intent)
	}

	private fun moveToMakeChannel() {
		val intent = Intent(requireContext(), MakeChannelActivity::class.java)
		startActivity(intent)
	}

	private fun handleEvent(event: HomeUiEvent) {
		when (event) {
			is HomeUiEvent.MoveToChannel -> moveToChannel(event.channelId)
			is HomeUiEvent.MoveToReadingBookShelf -> moveToReadingBookShelf()
		}
	}

}