package com.education.financetrackerrublik.ui.statistics

import com.education.financetrackerrublik.data.model.Category

data class CategoryStatistics(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val color: Int
) 