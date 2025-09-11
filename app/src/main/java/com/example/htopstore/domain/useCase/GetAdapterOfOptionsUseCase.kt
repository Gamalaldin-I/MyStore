package com.example.htopstore.domain.useCase

import android.content.Context
import android.widget.ArrayAdapter
import com.example.htopstore.R
import com.example.htopstore.domain.Category
import com.example.htopstore.domain.ExpenseCategory
import com.example.htopstore.domain.PaymentMethod
import java.util.Locale

abstract class GetAdapterOfOptionsUseCase {
    companion object{
        private fun getAdapter(context: Context,options:List<String>): ArrayAdapter<String>{
            val adapter = ArrayAdapter(context, R.layout.spinner_item, options)
            adapter.setDropDownViewResource(R.layout.spinner_dropdown)
            return adapter
        }
    fun getCategoriesAdapter(context: Context): ArrayAdapter<String> {
        val options = CategoryLocalManager.getCategories()
        return getAdapter(context,options)
    }
    fun getPaymentMethodAdapter(context: Context): ArrayAdapter<String> {
        val options = PaymentMethod.entries.map {it.method.capitalize(Locale.ROOT)}
        return getAdapter(context,options)
    }
    fun getExpenseCategoryAdapter(context: Context): ArrayAdapter<String> {
        val options = ExpenseCategory.entries.map {it.category.capitalize(Locale.ROOT)}
        return getAdapter(context,options)
    }
    }




}