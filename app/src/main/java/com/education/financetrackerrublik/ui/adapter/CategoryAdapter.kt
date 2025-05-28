package com.education.financetrackerrublik.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.Category

class CategoryAdapter(
    context: Context,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, R.layout.item_category, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_category, parent, false)

        val category = getItem(position)
        category?.let {
            view.findViewById<ImageView>(R.id.category_icon).setImageResource(it.iconResId)
            view.findViewById<TextView>(R.id.category_name).text = it.name
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                filterResults.values = categories
                filterResults.count = categories.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
} 