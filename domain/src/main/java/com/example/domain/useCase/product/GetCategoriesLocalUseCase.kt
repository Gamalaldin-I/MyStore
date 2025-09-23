package com.example.domain.useCase.product

import com.example.domain.model.category.Category
import java.util.Locale

class GetCategoriesLocalUseCase {
    private val categoriesInEnglish = Category.entries.map { it.eLabel }
    private val categoriesInArabic = Category.entries.map { it.aLabel }
    private fun  getCategoriesInEnglish(): List<String> {
        return categoriesInEnglish
    }

    private fun  getCategoriesInArabic(): List<String> {
        return categoriesInArabic
    }

    operator fun invoke(): List<String> {
        val local = Locale.getDefault().language
        var categories = emptyList<String>()
        categories = if (local == "ar") {
            getCategoriesInArabic()
        } else {
            getCategoriesInEnglish()
        }
        return getCategoriesInEnglish()
    }
}