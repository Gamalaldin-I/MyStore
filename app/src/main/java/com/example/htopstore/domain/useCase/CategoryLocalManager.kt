package com.example.htopstore.domain.useCase

import com.example.htopstore.domain.Category
import java.util.Locale

object CategoryLocalManager {
    private val categoriesInEnglish = Category.entries.map { it.eLabel }
    private val categoriesInArabic = Category.entries.map { it.aLabel }


     fun getCategoryNameLocal(category:String): String {
        val local = Locale.getDefault().language
        var catInLocal = ""
        if (local == "ar") {
            // search for category in enum arabic labels
            for (cat in Category.entries) {
                if (cat.aLabel == category || cat.eLabel == category.capitalize(Locale.ROOT)) {
                    catInLocal = cat.aLabel
                }
            }
        }
        else {
            // search for category in enum english labels
            for (cat in Category.entries) {
                if (cat.aLabel == category || cat.eLabel == category.capitalize(Locale.ROOT)) {
                    catInLocal = cat.eLabel}
            }
    }
        return catInLocal}

    private fun  getCategoriesInEnglish(): List<String> {
        return categoriesInEnglish
    }

    private fun  getCategoriesInArabic(): List<String> {
        return categoriesInArabic
    }

    fun getCategories(): List<String> {
        val local = Locale.getDefault().language
        var categories = emptyList<String>()
        categories = if (local == "ar") {
            getCategoriesInArabic()
        } else {
            getCategoriesInEnglish()
        }
        return categories
    }

}