package com.example.htopstore.util.helper

import android.content.Context
import android.widget.ArrayAdapter
import com.example.domain.model.category.ExpenseCategory
import com.example.domain.model.category.PaymentMethod
import com.example.domain.model.category.UserRoles
import com.example.domain.useCase.localize.GetCategoriesUseCase
import com.example.domain.util.DateHelper.DAY
import com.example.domain.util.DateHelper.MONTH
import com.example.domain.util.DateHelper.WEEK
import com.example.domain.util.DateHelper.YEAR
import com.example.htopstore.R
import java.util.Locale

object AutoCompleteHelper {

    val categories = GetCategoriesUseCase()

    private fun getAdapter(context: Context, options:List<String>): ArrayAdapter<String> {
        val adapter = ArrayAdapter(context, R.layout.spinner_item, options)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown)
        return adapter
        }
    fun getCategoriesAdapter(context: Context): ArrayAdapter<String> {
        val options = categories()
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
    fun getDurationAdapter(context: Context): ArrayAdapter<String> {
        val options = listOf(DAY,WEEK,MONTH,YEAR)
        return getAdapter(context,options)
    }
    fun getRolesAdapter(context: Context): ArrayAdapter<String>{
        val options = UserRoles.entries.map {it.roleName.capitalize(Locale.ROOT)}
        return getAdapter(context,options)
    }

    }