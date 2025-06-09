package com.education.financetrackerrublik.ui.categories

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.databinding.DialogEditCategoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryEditDialog(
    private val context: Context,
    private val category: Category? = null,
    private val onSave: (String, Int) -> Unit
) {
    private val binding = DialogEditCategoryBinding.inflate(LayoutInflater.from(context))
    private var selectedIconResId = category?.iconResId ?: R.drawable.ic_other

    private val availableIcons = listOf(
        IconGridAdapter.IconItem(R.drawable.ic_food, "Еда"),
        IconGridAdapter.IconItem(R.drawable.ic_transport, "Транспорт"),
        IconGridAdapter.IconItem(R.drawable.ic_shopping, "Покупки"),
        IconGridAdapter.IconItem(R.drawable.ic_home, "Дом"),
        IconGridAdapter.IconItem(R.drawable.ic_entertainment, "Развлечения"),
        IconGridAdapter.IconItem(R.drawable.ic_health, "Здоровье"),
        IconGridAdapter.IconItem(R.drawable.ic_education, "Образование"),
        IconGridAdapter.IconItem(R.drawable.ic_work, "Работа"),
        IconGridAdapter.IconItem(R.drawable.ic_bills, "Счета"),
        IconGridAdapter.IconItem(R.drawable.ic_gifts, "Подарки"),
        IconGridAdapter.IconItem(R.drawable.ic_clothes, "Одежда"),
        IconGridAdapter.IconItem(R.drawable.ic_cafe, "Кафе"),
        IconGridAdapter.IconItem(R.drawable.ic_beauty, "Красота"),
        IconGridAdapter.IconItem(R.drawable.ic_sports, "Спорт"),
        IconGridAdapter.IconItem(R.drawable.ic_pets, "Питомцы"),
        IconGridAdapter.IconItem(R.drawable.ic_investment, "Инвестиции"),
        IconGridAdapter.IconItem(R.drawable.ic_bonus, "Бонусы"),
        IconGridAdapter.IconItem(R.drawable.ic_other, "Другое")
    )

    init {
        setupViews()
    }

    private fun setupViews() {
        // Заполняем поле названия если это редактирование
        category?.let {
            binding.nameEdit.setText(it.name)
        }

        // Настраиваем сетку иконок
        binding.iconGrid.layoutManager = GridLayoutManager(context, 4)
        val adapter = IconGridAdapter(
            icons = availableIcons,
            initialIconResId = selectedIconResId
        ) { iconResId ->
            selectedIconResId = iconResId
        }
        binding.iconGrid.adapter = adapter
    }

    fun show() {
        MaterialAlertDialogBuilder(context)
            .setTitle(if (category == null) "Новая категория" else "Редактирование категории")
            .setView(binding.root)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = binding.nameEdit.text.toString().trim()
                if (name.isNotEmpty()) {
                    onSave(name, selectedIconResId)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
} 