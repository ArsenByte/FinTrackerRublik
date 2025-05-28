package com.education.financetrackerrublik.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import com.education.financetrackerrublik.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private val onDeleteClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionWithCategory, TransactionsAdapter.ViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        fun bind(transactionWithCategory: TransactionWithCategory) {
            binding.apply {
                categoryIcon.setImageResource(transactionWithCategory.category.iconResId)
                categoryName.text = transactionWithCategory.category.name
                date.text = dateFormat.format(transactionWithCategory.transaction.date)
                
                val amountText = if (transactionWithCategory.transaction.type == TransactionType.EXPENSE) {
                    "- ${transactionWithCategory.transaction.amount} ₽"
                } else {
                    "+ ${transactionWithCategory.transaction.amount} ₽"
                }
                amount.text = amountText
                amount.setTextColor(
                    binding.root.context.getColor(
                        if (transactionWithCategory.transaction.type == TransactionType.EXPENSE) {
                            R.color.expense_color
                        } else {
                            R.color.income_color
                        }
                    )
                )

                if (transactionWithCategory.transaction.description.isNotEmpty()) {
                    note.text = transactionWithCategory.transaction.description
                    note.visibility = android.view.View.VISIBLE
                } else {
                    note.visibility = android.view.View.GONE
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(transactionWithCategory)
                }
            }
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

