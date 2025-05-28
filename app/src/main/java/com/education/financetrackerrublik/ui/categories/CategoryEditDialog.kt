package com.education.financetrackerrublik.ui.categories

import android.content.Context
import android.view.LayoutInflater
import com.education.financetrackerrublik.R
import com.education.financetrackerrublik.data.model.Category
import com.education.financetrackerrublik.databinding.DialogEditCategoryBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoryEditDialog(
    private val context: Context,
    private val category: Category? = null,
    private val onSave: (String, Int) -> Unit
) {
    private val binding = DialogEditCategoryBinding.inflate(LayoutInflater.from(context))
    private var selectedIconResId = category?.iconResId ?: R.drawable.ic_other

    private val availableIcons = listOf(
        R.drawable.ic_food to "Еда",
        R.drawable.ic_transport to "Транспорт",
        R.drawable.ic_shopping to "Покупки",
        R.drawable.ic_home to "Дом",
        R.drawable.ic_entertainment to "Развлечения",
        R.drawable.ic_health to "Здоровье",
        R.drawable.ic_education to "Образование",
        R.drawable.ic_work to "Работа",
        R.drawable.ic_other to "Другое"
    )

    init {
        setupViews()
    }

    private fun setupViews() {
        // Заполняем поле названия если это редактирование
        category?.let {
            binding.nameEdit.setText(it.name)
        }

        // Создаем чипы для каждой иконки
        availableIcons.forEach { (iconResId, description) ->
            val chip = Chip(context).apply {
                setChipIconResource(iconResId)
                isCheckable = true
                isChecked = iconResId == selectedIconResId
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedIconResId = iconResId
                    }
                }
            }
            binding.iconGroup.addView(chip)
        }
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