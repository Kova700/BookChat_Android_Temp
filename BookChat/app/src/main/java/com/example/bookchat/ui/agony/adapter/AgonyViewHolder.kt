package com.example.bookchat.ui.agony.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.bookchat.databinding.ItemAgonyDataBinding
import com.example.bookchat.databinding.ItemAgonyDataEditingBinding
import com.example.bookchat.databinding.ItemAgonyFirstBinding
import com.example.bookchat.databinding.ItemAgonyHeaderBinding
import com.example.bookchat.ui.agony.model.AgonyListItem

sealed class AgonyViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
	abstract fun bind(agonyListItem: AgonyListItem)
}

class AgonyHeaderItemViewHolder(
	private val binding: ItemAgonyHeaderBinding
) : AgonyViewHolder(binding) {
	override fun bind(agonyListItem: AgonyListItem) {
		binding.bookShelfItem = (agonyListItem as AgonyListItem.Header).bookShelfItem
	}
}

class AgonyFirstItemViewHolder(
	private val binding: ItemAgonyFirstBinding,
	private val onFirstItemClick: (() -> Unit)?,
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onFirstItemClick?.invoke()
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {}
}

class AgonyDataItemViewHolder(
	private val binding: ItemAgonyDataBinding,
	private val onItemClick: ((Int) -> Unit)?
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemClick?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {
		binding.agonyListItem = (agonyListItem as AgonyListItem.Item)
	}
}

class AgonyDataEditingItemViewHolder(
	private val binding: ItemAgonyDataEditingBinding,
	private val onItemSelect: ((Int) -> Unit)?,
) : AgonyViewHolder(binding) {
	init {
		binding.root.setOnClickListener {
			onItemSelect?.invoke(absoluteAdapterPosition)
		}
	}

	override fun bind(agonyListItem: AgonyListItem) {
		val item = (agonyListItem as AgonyListItem.Item)
		binding.agonyListItem = item
	}
}