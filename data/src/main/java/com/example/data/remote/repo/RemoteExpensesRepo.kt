package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.DeleteBody
import com.example.domain.model.Expense
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class RemoteExpensesRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
){
    val c = Constants
    companion object{
        private const val TABLE_NAME = "expenses"
        private const val TAG = "Expenses"
    }


    suspend fun addExpense(expense: Expense,onLocal:suspend ()->Unit):Pair<Boolean,String>{
        try{
            supabase.from(TABLE_NAME).insert(expense)
            onLocal()
            return Pair(true,c.EXPENSE_ADDED_MESSAGE)

        }catch(e: Exception){
            Log.e(TAG,"error adding ${e.message}")
            return Pair(false,c.EXPENSE_ADDED_FAILED_MESSAGE)
        }
    }
    suspend fun deleteExpenseById(id: String,onLocal:suspend ()->Unit): Pair<Boolean,String>{
        try {
            supabase.from(TABLE_NAME).update(
                DeleteBody(
                    deleted = true,
                    lastUpdate = DateHelper.getCurrentTimestampTz()
                )
            ) {
                filter {
                    eq("id", id)
                }
            }
            onLocal()
            return Pair(true, c.EXPENSE_DELETED_MESSAGE)
        }catch (e: Exception){
            Log.e(TAG,"error deleting ${e.message}")
            return Pair(false, c.EXPENSE_DELETED_FAILED_MESSAGE)
        }
    }

    suspend fun getExpenses(onLocal: suspend (list: List<Expense>) -> Unit): Boolean{
        try {
            val expenses = if (pref.getLastExpensesUpdate().isEmpty()){
                //get all expenses for the firs time
                supabase.from(TABLE_NAME).select().decodeList<Expense>()
            }else{
                //get only the new expenses
                supabase.from(TABLE_NAME).select{
                    filter {
                        gt("lastUpdate",pref.getLastExpensesUpdate())
                    }
                }.decodeList<Expense>()
            }
            if(expenses.isEmpty()){
                return true
            }
            onLocal(expenses)
            pref.setLastExpensesUpdate()
            return true
        }catch (e: Exception){
            Log.e(TAG,"error getting ${e.message}")
            return false
            }
        }


}