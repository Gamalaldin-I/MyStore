package com.example.domain.useCase.localize

import com.example.domain.model.category.Category
import java.util.Locale

class GetCategoryLocalName(){
    operator fun invoke(category:String): String {
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
        return catInLocal
    }
}