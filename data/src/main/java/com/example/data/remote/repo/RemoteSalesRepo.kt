package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.DeleteBody
import com.example.domain.model.Product
import com.example.domain.model.SoldProduct
import com.example.domain.model.remoteModels.ProductQuantityUpdate
import com.example.domain.model.remoteModels.SoldProductUpdateBody
import com.example.domain.util.DateHelper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class RemoteSalesRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val networkManager: NetworkHelperInterface
) {

    companion object {
        private const val SALES = "soldProducts"
        private const val PRODUCTS = "products"
        private const val TAG = "REMOTE_SALES_REPO"
    }

    // --------------------------------------------------
    // ADD SALES
    // --------------------------------------------------
    suspend fun addSales(
        sales: List<SoldProduct>,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        if (sales.isEmpty()) {
            return Pair(true, "No new sales")
        }

        return try {
            supabase.from(SALES).insert(sales)
            onResult()
            Pair(true, "Sales added successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding sales: ${e.message}", e)
            Pair(false, "Error adding sales")
        }
    }

    // --------------------------------------------------
    // FETCH SALES
    // --------------------------------------------------
    suspend fun fetchSales(
        onResult: suspend (List<SoldProduct>) -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        return try {
            val lastUpdate = pref.getLastSalesUpdate()

            val sales = if (lastUpdate.isEmpty()) {
                // First time → fetch all
                supabase.from(SALES)
                    .select()
                    .decodeList<SoldProduct>()
            } else {
                // Fetch only new sales
                supabase.from(SALES)
                    .select {
                        filter { gt("lastUpdate", lastUpdate) }
                    }
                    .decodeList<SoldProduct>()
            }

            if (sales.isEmpty()) {
                return Pair(true, "No new sales")
            }

            onResult(sales)
            pref.setLastSalesUpdate()
            Pair(true, "Sales fetched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sales: ${e.message}", e)
            Pair(false, "Error fetching sales")
        }
    }

    // --------------------------------------------------
    // UPDATE PRODUCT QUANTITY AFTER SELL OR RETURN
    // --------------------------------------------------
    suspend fun updateQuantityAvailable(
        id: String,
        count: Int,
        isSell: Boolean,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }
        val currentProduct = supabase.from(PRODUCTS).select{filter { eq("id", id) }}.decodeSingle<Product>()

        val updateBody = ProductQuantityUpdate(
            count = if (isSell) (currentProduct.count-count) else (currentProduct.count+count),
            soldCount = if (isSell) (currentProduct.soldCount+count) else (currentProduct.soldCount-count),
            lastUpdate = DateHelper.getCurrentTimestampTz()
        )

        return try {
            supabase.from(PRODUCTS).update(updateBody) {
                filter { eq("id", id) }
            }
            onResult()
            Pair(true, "Quantity updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating product quantity: ${e.message}", e)
            Pair(false, "Error updating quantity")
        }
    }

    // --------------------------------------------------
    // SOFT DELETE SOLD PRODUCT
    // --------------------------------------------------
    suspend fun deleteSoldProduct(
        id: String,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }

        val deleteBody = DeleteBody(
            deleted = true,
            lastUpdate = DateHelper.getCurrentTimestampTz()
        )

        return try {
            supabase.from(SALES).update(deleteBody) {
                filter { eq("id", id) }
            }
            onResult()
            Pair(true, "Sold product deleted successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting sold product: ${e.message}", e)
            Pair(false, "Error deleting sold product")
        }
    }

    // --------------------------------------------------
    // UPDATE SOLD PRODUCT AFTER RETURN
    // --------------------------------------------------
    suspend fun updateSoldProductAfterReturn(
        id: String,
        quantity: Int,
        onResult: suspend () -> Unit
    ): Pair<Boolean, String> {

        if (!networkManager.isConnected()) {
            return Pair(false, "No internet connection")
        }
        val currentProduct = supabase.from("soldProducts").select{filter { eq("id", id)}}.decodeSingle<SoldProduct>()

        val updateBody = SoldProductUpdateBody(
            quantity = (currentProduct.quantity-quantity),
            lastUpdate = DateHelper.getCurrentTimestampTz()
        )

        return try {
            supabase.from(SALES).update(updateBody) {
                filter { eq("id", id) }
            }
            onResult()
            Pair(true, "Sold product updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating sold product: ${e.message}", e)
            Pair(false, "Error updating sold product")
        }
    }
}
