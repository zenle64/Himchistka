package com.example.drycleaning.ui.clients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.drycleaning.data.entity.Client
import com.example.drycleaning.databinding.ItemClientBinding

/** Адаптер списка клиентов */
class ClientAdapter(
    private val onItemClick: (Client) -> Unit
) : ListAdapter<Client, ClientAdapter.ClientViewHolder>(ClientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClientViewHolder(
        private val binding: ItemClientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(client: Client) {
            binding.apply {
                tvClientName.text = client.fullName
                tvClientPhone.text = client.phone
                tvClientEmail.text = client.email.ifBlank { "—" }
                root.setOnClickListener { onItemClick(client) }
            }
        }
    }
}

class ClientDiffCallback : DiffUtil.ItemCallback<Client>() {
    override fun areItemsTheSame(oldItem: Client, newItem: Client) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Client, newItem: Client) = oldItem == newItem
}
