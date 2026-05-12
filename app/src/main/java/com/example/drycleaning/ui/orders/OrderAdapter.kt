package com.example.drycleaning.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.drycleaning.data.entity.Order
import com.example.drycleaning.databinding.ItemOrderBinding
import com.example.drycleaning.util.displayOrderNumber
import com.example.drycleaning.util.toCurrencyString
import com.example.drycleaning.util.toColorRes
import com.example.drycleaning.util.toDateString
import com.example.drycleaning.util.toDisplayString

/** Адаптер списка заказов */
class OrderAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.apply {
                tvOrderNumber.text = order.displayOrderNumber()
                tvClientName.text = order.clientName
                tvItemType.text = "${order.itemType} — ${order.serviceType}"
                tvPrice.text = order.price.toCurrencyString()
                tvDueDate.text = "Выдача: ${order.dueDate.toDateString()}"
                tvStatus.text = order.status.toDisplayString()
                tvStatus.setTextColor(ContextCompat.getColor(root.context, order.status.toColorRes()))
                root.setOnClickListener { onItemClick(order) }
            }
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
}
