package com.education.financetrackerrublik.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.TransactionType
import com.education.financetrackerrublik.data.model.TransactionWithCategory
import com.education.financetrackerrublik.databinding.ItemDateHeaderBinding
import com.education.financetrackerrublik.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter(
    private val onDeleteClick: (TransactionWithCategory) -> Unit
) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(TransactionListItemDiffCallback()) {

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.DateHeader -> VIEW_TYPE_HEADER
            is TransactionListItem.TransactionItem -> VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateHeaderViewHolder(binding)
            }
            VIEW_TYPE_TRANSACTION -> {
                val binding = ItemTransactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TransactionViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }

    inner class DateHeaderViewHolder(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(header: TransactionListItem.DateHeader) {
            binding.dateText.text = dateFormat.format(header.date)
            binding.incomeAmount.text = numberFormat.format(header.totalIncome)
            binding.expenseAmount.text = numberFormat.format(header.totalExpense)
        }
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnLongClickListener {
                val transaction = (getItem(adapterPosition) as? TransactionListItem.TransactionItem)?.transaction
                transaction?.let { onDeleteClick(it) }
                true
            }
        }

        fun bind(transactionWithCategory: TransactionWithCategory) {
            binding.apply {
                categoryIcon.setImageResource(transactionWithCategory.category.iconResId)
                categoryName.text = transactionWithCategory.category.name
                date.text = dateFormat.format(transactionWithCategory.transaction.date)
                
                val amountText = if (transactionWithCategory.transaction.type == TransactionType.EXPENSE) {
                    "- ${numberFormat.format(transactionWithCategory.transaction.amount)}"
                } else {
                    "+ ${numberFormat.format(transactionWithCategory.transaction.amount)}"
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
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }
}

private class TransactionListItemDiffCallback : DiffUtil.ItemCallback<TransactionListItem>() {
    override fun areItemsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
        return when {
            oldItem is TransactionListItem.DateHeader && newItem is TransactionListItem.DateHeader ->
                oldItem.date.time == newItem.date.time
            oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem ->
                oldItem.transaction.transaction.id == newItem.transaction.transaction.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: TransactionListItem, newItem: TransactionListItem): Boolean {
        return oldItem == newItem
    }
}

