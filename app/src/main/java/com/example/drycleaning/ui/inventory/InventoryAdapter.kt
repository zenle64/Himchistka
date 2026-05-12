package com.example.drycleaning.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.drycleaning.data.entity.InventoryItem
import com.example.drycleaning.databinding.ItemInventoryBinding

/** Адаптер списка расходных материалов на складе */
class InventoryAdapter(
    private val onItemClick: (InventoryItem) -> Unit
) : ListAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder>(InventoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(
        private val binding: ItemInventoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InventoryItem) {
            binding.apply {
                tvItemName.text = item.name
                tvQuantity.text = "${item.quantity} ${item.unit}"
                tvMinQuantity.text = "Мин: ${item.minQuantity} ${item.unit}"

                val isLow = item.quantity <= item.minQuantity
                if (isLow) {
                    tvQuantity.setTextColor(root.context.getColor(android.R.color.holo_red_dark))
                    tvWarning.visibility = android.view.View.VISIBLE
                } else {
                    tvQuantity.setTextColor(root.context.getColor(android.R.color.holo_green_dark))
                    tvWarning.visibility = android.view.View.GONE
                }

                root.setOnClickListener { onItemClick(item) }
            }
        }
    }
}

class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryItem>() {
    override fun areItemsTheSame(oldItem: InventoryItem, newItem: InventoryItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: InventoryItem, newItem: InventoryItem) = oldItem == newItem
}
