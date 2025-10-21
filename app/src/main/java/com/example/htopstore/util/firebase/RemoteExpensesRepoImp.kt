package com.example.htopstore.util.firebase

import com.example.data.remote.RemoteExpensesRepo
import com.example.domain.model.Expense
import com.google.firebase.firestore.FirebaseFirestore

class RemoteExpensesRepoImp(val db: FirebaseFirestore): RemoteExpensesRepo {
    override fun addExpense(expense: Expense) {
        TODO("Not yet implemented")
    }

    override fun deleteExpenseById(id: String) {
        TODO("Not yet implemented")
    }

    override fun getExpenses(): List<Expense> {
        TODO("Not yet implemented")
    }

    override fun addListOfExpenses(expenses: List<Expense>) {
        TODO("Not yet implemented")
    }
}