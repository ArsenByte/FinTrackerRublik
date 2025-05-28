package com.education.financetrackerrublik.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import com.education.financetrackerrublik.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter(
    private val onDeleteClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

        init {
            binding.root.setOnLongClickListener {
                onDeleteClick(getItem(adapterPosition))
                true
            }
        }

        fun bind(transactionWithCategory: TransactionWithCategory) {
            binding.apply {
                val transaction = transactionWithCategory.transaction
                val category = transactionWithCategory.category

                amount.text = String.format("%.2f ₽", transaction.amount)
                date.text = dateFormat.format(transaction.date)
                categoryName.text = category.name
                categoryIcon.setImageResource(category.iconResId)
                
                // Set text color based on transaction type
                val textColor = if (transaction.type == TransactionType.EXPENSE) {
                    android.graphics.Color.RED
                } else {
                    android.graphics.Color.GREEN
                }
                amount.setTextColor(textColor)
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<TransactionWithCategory>() {
        override fun areItemsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
            return oldItem.transaction.id == newItem.transaction.id
        }

        override fun areContentsTheSame(oldItem: TransactionWithCategory, newItem: TransactionWithCategory): Boolean {
            return oldItem == newItem
        }
    }
} 