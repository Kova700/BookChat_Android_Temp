package com.example.bookchat.ui.agony

import com.example.bookchat.domain.model.BookShelfItem
import com.example.bookchat.ui.agony.model.AgonyListItem

data class AgonyUiState(
	val uiState: UiState,
	val bookshelfItem: BookShelfItem,
	val agonies: List<AgonyListItem>
) {
	enum class UiState {
		SUCCESS,
		LOADING,
		ERROR,
		EMPTY,
		EDITING
	}

	companion object {
		val DEFAULT = AgonyUiState(
			uiState = UiState.LOADING,
			bookshelfItem = BookShelfItem.DEFAULT,
			agonies = emptyList()
		)
	}
}

sealed class AgonyEvent {
	object MoveToBack : AgonyEvent()
	object RenewItemViewMode : AgonyEvent()
	data class OpenBottomSheetDialog(val bookshelfItemId: Long) : AgonyEvent()

	data class MoveToAgonyRecord(
		val bookshelfItemId: Long,
		val agonyListItemId: Long
	) : AgonyEvent()

	data class MakeToast(
		val stringId: Int
	) : AgonyEvent()

}
