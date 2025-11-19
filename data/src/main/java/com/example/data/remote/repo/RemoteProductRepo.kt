package com.example.data.remote.repo

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.DeleteBody
import com.example.domain.model.Product
import com.example.domain.util.DateHelper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class RemoteProductRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context,
    private val networkHelper: NetworkHelperInterface
) {
    companion object {
        private const val PRODUCT_BUCKET = "Products"
        private const val PRODUCTS = "products"
        private const val ID = "id"
        private const val TAG = "SupabaseHelper"
    }

    // ==============================
    // IMAGE UPLOAD/REMOVE FUNCTIONS
    // ==============================

    private suspend fun uploadImage(uri: android.net.Uri, path: String, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return null
            val bucket = supabase.storage.from(path)
            bucket.upload(fileName, bytes, upsert = true)
            "https://ayoanqjzciolnahljauc.supabase.co/storage/v1/object/public/$path/$fileName"
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            null
        }
    }


    private suspend fun removePhoto(fileName: String, path: String): Boolean {
        return try {
            val bucket = supabase.storage.from(path)
            bucket.delete(fileName)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing photo: ${e.message}", e)
            false
        }
    }


    // ==============================
    // PRODUCT FUNCTIONS
    // ==============================

    suspend fun addProduct(product: Product, onResult: suspend (Product?) -> Unit) {
        if (!networkHelper.isConnected()) {
            onResult(null)
            return
        }
        try {
            val inserted = product.copy(storeId = pref.getStore().id)
            val fileName = "${inserted.id}.jpg"
            val uri = inserted.productImage.toUri()
            val url = uploadImage(uri, PRODUCT_BUCKET, fileName)
            inserted.productImage = url.orEmpty()
            supabase.from(PRODUCTS).insert(inserted)
            onResult(inserted)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}")
            onResult(null)
        }
    }

    suspend fun deleteProduct(id: String, onResult: suspend () -> Unit):Pair<Boolean,String>{
        val success = Pair(true,"Product deleted successfully")
        val fail = Pair(false,"check your internet connection")
        try {
            val result = removePhoto("$id.jpg", PRODUCT_BUCKET)
            if (!result) return fail
            supabase.from(PRODUCTS).update(
                DeleteBody(
                    lastUpdate = DateHelper.getCurrentTimestampTz(),
                    deleted = true
                )
            ) {
                filter { eq(ID, id) }
            }
            onResult()
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}", e)
            return fail
        }
    }

    suspend fun updateProduct(product: Product, onResult: suspend (Product?) -> Unit) {
        //should assign the  key to the product
        val updatedProduct = product.copy(storeId = pref.getStore().id)
        if (!networkHelper.isConnected()) {
            onResult(null)
            return
        }
        try {
            supabase.from(PRODUCTS).update(updatedProduct) {
                filter { eq(ID, updatedProduct.id)}
            }
            onResult(updatedProduct)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}", e)
            onResult(null)
        }
    }

    suspend fun observeAllProductsWithLastUpdateAndDeleted(): Pair<List<Product>, String> {
        if (!networkHelper.isConnected()) {
            return Pair(emptyList(), "No internet connection")
        }
        try {
            if(pref.getLastProductsUpdate().isEmpty()){
                //get all products for first time after login
                val products = supabase.from(PRODUCTS).select().decodeList<Product>()
                Log.d(TAG, "products: all products ${products.size}")
                return submitList(products)
            }else{
                //get all products that updated since last update
                val updatedProducts = supabase.from(PRODUCTS).select{
                    filter {
                        gte("lastUpdate", pref.getLastProductsUpdate())
                    }
                }.decodeList<Product>()
                Log.d(TAG, "products: updated products ${updatedProducts.size}")
                return submitList(updatedProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all products: ${e.message}", e)
            return Pair(emptyList(), "Error getting all products")
        }
    }
    private fun submitList(products: List<Product>): Pair<List<Product>, String> {
        if (products.isEmpty()) {
            return Pair(emptyList(), "No products or updates found")
        }
        //very important to update the last update time
        pref.setLastProductsUpdate()
        return Pair(products, "New products and updates found")
    }

    suspend fun getProductById(id: String): Product? {
        return try {
            supabase.from(PRODUCTS).select {
                filter { eq(ID, id) }
            }.decodeSingle<Product>()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting product by id: ${e.message}", e)
            null
        }
    }



}
