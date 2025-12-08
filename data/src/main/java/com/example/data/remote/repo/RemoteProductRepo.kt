package com.example.data.remote.repo

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.DeleteBody
import com.example.domain.model.Product
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.util.DateHelper
import com.example.domain.util.NotificationManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class RemoteProductRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context,
    private val networkHelper: NetworkHelperInterface,
    private val notSender: InsertNotificationUseCase
) {

    companion object {
        private const val PRODUCT_BUCKET = "Products"
        private const val PRODUCTS = "products"
        private const val ID = "id"
        private const val TAG = "SupabaseHelper"
    }

    // ==============================
    // IMAGE UPLOAD / REMOVE FUNCTIONS
    // ==============================
    private suspend fun uploadImage(
        uri: android.net.Uri,
        path: String,
        fileName: String
    ): String? {
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

    private suspend fun removePhoto(fileName: String, path: String): Boolean{
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

    // --- ADD PRODUCT ---
    suspend fun addProduct(
        product: Product,
        onResult: suspend (Product?, String) -> Unit
    ) {
        if (!networkHelper.isConnected()) {
            onResult(null, "No internet connection")
            return
        }

        try {
            val inserted = product.copy(storeId = pref.getStore().id)
            val fileName = "${inserted.id}.jpg"
            val uri = inserted.productImage.toUri()
            val url = uploadImage(uri, PRODUCT_BUCKET, fileName)
            inserted.productImage = url.orEmpty()

            supabase.from(PRODUCTS).insert(inserted)
            val addedNot = NotificationManager.createAddProductNotification(
                pref.getUser(),
                pref.getStore().id,
                product
            )
            notSender(addedNot)


            onResult(inserted, "Product added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}", e)
            onResult(null, "Failed to add product")
        }
    }

    // --- DELETE PRODUCT ---
    suspend fun deleteProduct(
        id: String,
        onResult: suspend () -> Unit
    ):Pair<Boolean,String>{
        if (!networkHelper.isConnected()) {
            return Pair(false, "No internet connection")
        }
        val deleted =removePhoto("$id.jpg", PRODUCT_BUCKET)
        if(!deleted){
            return Pair(false, "Failed to delete image")
        }


        try {
            supabase.from(PRODUCTS).update(
                DeleteBody(
                    lastUpdate = DateHelper.getCurrentTimestampTz(),
                    deleted = true
                )
            ) {
                filter { eq(ID, id) }
            }
            val deletedNot = NotificationManager.createDeleteProductNotification(
                pref.getUser(),
                pref.getStore().id,
                productId = id
            )
            notSender(deletedNot)


            onResult()
            return Pair(true, "Product deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}", e)
           return Pair(false, "Failed to delete product or not allowed by server")
        }
    }

    // --- UPDATE PRODUCT ---
    suspend fun updateProduct(
        product: Product,
        onResult: suspend (Product?, String) -> Unit
    ) {
        if (!networkHelper.isConnected()) {
            onResult(null, "No internet connection")
            return
        }

        val updatedProduct = product.copy(storeId = pref.getStore().id)

        try {
            supabase.from(PRODUCTS).update(updatedProduct) {
                filter { eq(ID, updatedProduct.id) }
            }
            val updatedNot = NotificationManager.createUpdateProductNotification(
                pref.getUser(),
                pref.getStore().id,
                product
            )
            notSender(updatedNot)

            onResult(updatedProduct, "Product updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}", e)
            onResult(null, "Failed to update product or not allowed by server")
        }
    }

    // --- GET ALL PRODUCTS WITH LAST UPDATE ---
    suspend fun observeAllProductsWithLastUpdateAndDeleted(): Pair<List<Product>, String> {
        if (!networkHelper.isConnected()) {
            return Pair(emptyList(), "No internet connection")
        }

        return try {
            val products = if (pref.getLastProductsUpdate().isEmpty()) {
                // First fetch after login
                supabase.from(PRODUCTS).select {
                    filter { eq("storeId", pref.getStore().id) }
                }.decodeList<Product>()
            } else {
                // Fetch only updated products
                supabase.from(PRODUCTS).select {
                    filter {
                        eq("storeId", pref.getStore().id)
                        gte("lastUpdate", pref.getLastProductsUpdate())
                    }
                }.decodeList<Product>()
            }

            if (products.isEmpty()) {
                Pair(emptyList(), "No products or updates found")
            } else {
                pref.setLastProductsUpdate()
                Pair(products, "New products and updates found")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products: ${e.message}", e)
            Pair(emptyList(), "Failed to fetch products")
        }
    }

    // --- GET PRODUCT BY ID ---
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
