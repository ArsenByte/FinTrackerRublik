package com.education.financetrackerrublik.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.education.financetrackerrublik.databinding.ItemCategoryStatBinding
import com.education.financetrackerrublik.ui.statistics.CategoryStatistics
import java.text.NumberFormat
import java.util.Locale

class CategoryStatisticsAdapter : ListAdapter<CategoryStatistics, CategoryStatisticsAdapter.ViewHolder>(
    CategoryStatisticsDiffCallback()
) {
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryStatBinding.inflate(
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
        private val binding: ItemCategoryStatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryStatistics) {
            binding.apply {
                colorIndicator.setBackgroundColor(item.color)
                categoryName.text = item.category.name
                amount.text = numberFormat.format(item.amount)
                percentage.text = "%.1f%%".format(item.percentage)
            }
        }
    }
}

private class CategoryStatisticsDiffCallback : DiffUtil.ItemCallback<CategoryStatistics>() {
    override fun areItemsTheSame(oldItem: CategoryStatistics, newItem: CategoryStatistics): Boolean {
        return oldItem.category.id == newItem.category.id
    }

    override fun areContentsTheSame(oldItem: CategoryStatistics, newItem: CategoryStatistics): Boolean {
        return oldItem == newItem
    }
} 