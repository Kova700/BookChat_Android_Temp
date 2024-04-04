package com.example.bookchat.ui.agonyrecord.model

import com.example.bookchat.domain.model.Agony

sealed interface AgonyRecordListItem {
	fun getCategoryId(): Long {
		return when (this) {
			is Header -> HEADER_ITEM_STABLE_ID
			is FirstItem -> FIRST_ITEM_STABLE_ID
			is Item -> recordId
		}
	}

	data class Header(
		val agony: Agony
	) : AgonyRecordListItem

	data class FirstItem(
		val state: ItemState = ItemState.Success(),
	) : AgonyRecordListItem

	data class Item(
		val recordId: Long,
		val title: String,
		val content: String,
		val createdAt: String,
		val state: ItemState = ItemState.Success(),
	) : AgonyRecordListItem

	sealed interface ItemState {
		data class Success(val isSwiped: Boolean = false) : ItemState
		object Loading : ItemState
		data class Editing(
			var titleBeingEdited: String = "",
			var contentBeingEdited: String = "",
		) : ItemState
	}

	companion object {
		const val HEADER_ITEM_STABLE_ID = -1L
		const val FIRST_ITEM_STABLE_ID = -2L
	}
}