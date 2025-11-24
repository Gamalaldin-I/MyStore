package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.Bill
import com.example.domain.model.DeleteBody
import com.example.domain.model.remoteModels.BillCashUpdate
import com.example.domain.util.DateHelper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class RemoteBillRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val networkManager: NetworkHelperInterface
) {

    companion object {
        private const val BILLS = "bill"
        private const val TAG = "REMOTE_BILL_REPO"
    }

    // --------------------------------------------------
    // ADD BILL
    // --------------------------------------------------
    suspend fun addBill(
        bill: Bill,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        return try {
            val inserted = bill.copy(
                storeId = pref.getStore().id,
                userId = pref.getUser().id
            )

            supabase.from(BILLS).insert(inserted)
            onResult()

            Pair(true, "Bill added successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding bill: ${e.message}", e)
            Pair(false, "Error adding bill")
        }
    }

    // --------------------------------------------------
    // FETCH BILLS
    // --------------------------------------------------
    suspend fun fetchBills(
        onResult: suspend (List<Bill>) -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        return try {
            val lastUpdate = pref.getLastBillsUpdate()

            val bills = if (lastUpdate.isEmpty()) {
                // First fetch → get all
                supabase.from(BILLS)
                    .select()
                    .decodeList<Bill>()
            } else {
                // Fetch only new data
                supabase.from(BILLS)
                    .select {
                        filter { gt("lastUpdate", lastUpdate) }
                    }
                    .decodeList<Bill>()
            }

            if (bills.isEmpty()) {
                return Pair(true, "No new bills")
            }

            onResult(bills)
            pref.setLastBillsUpdate()

            Pair(true, "Bills fetched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bills: ${e.message}", e)
            Pair(false, "Error fetching bills")
        }
    }

    // --------------------------------------------------
    // UPDATE BILL CASH
    // --------------------------------------------------
    suspend fun updateBillCashAfterReturn(
        id: String,
        cash: Double,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        return try {
            val currentBill = supabase.from(BILLS).select{
                filter { eq("id", id) }
            }.decodeSingle<Bill>()


            supabase.from(BILLS).update(
                    BillCashUpdate(
                        totalCash = currentBill.totalCash-cash,
                        lastUpdate = DateHelper.getCurrentTimestampTz(),
                        userId = pref.getUser().id)) { filter { eq("id", id)
                        }
            }

            onResult()
            Pair(true, "Bill updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating bill cash: ${e.message}", e)
            Pair(false, "Error updating bill")
        }
    }


    // --------------------------------------------------
    // DELETE BILL (Soft Delete)
    // --------------------------------------------------
    suspend fun deleteBill(
        id: String,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        return try {
            supabase.from(BILLS).update(
                DeleteBody(
                    deleted = true,
                    lastUpdate = DateHelper.getCurrentTimestampTz()
                )
            ) {
                filter { eq("id", id) }
            }

            onResult()
            Pair(true, "Bill deleted successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting bill: ${e.message}", e)
            Pair(false, "Error deleting bill")
        }
    }
    suspend fun isTheBillFound(id:String): Boolean{
        val bill= supabase.from(BILLS).select{
            filter { eq("id", id) }}.decodeSingle<Bill>()
        return bill.storeId.isNotEmpty()
    }
}
