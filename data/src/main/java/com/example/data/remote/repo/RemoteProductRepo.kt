package com.example.data.remote.repo

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeOldRecord
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteProductRepo(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context
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

    suspend fun addProduct(product: Product, onResult: suspend (Product) -> Unit) {
        try {
            val inserted = product
            inserted.storeId = pref.getStore().id
            val fileName = "${product.id}.jpg"
            val uri = product.productImage.toUri()
            val url = uploadImage(uri, PRODUCT_BUCKET, fileName)
            product.productImage = url.orEmpty()
            supabase.from(PRODUCTS).insert(inserted)
            onResult(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}", e)
        }
    }

    suspend fun deleteProduct(id: String, onResult: suspend (String) -> Unit) {
        try {
            val result = removePhoto("$id.jpg", PRODUCT_BUCKET)
            if (!result) throw Exception("Failed to delete image")
            supabase.from(PRODUCTS).delete {
                filter { eq(ID, id) }
            }
            onResult(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}", e)
        }
    }

    suspend fun updateProduct(product: Product, onResult: suspend (Product) -> Unit) {
        try {
            supabase.from(PRODUCTS).update(product) {
                filter { eq(ID, product.id) }
            }
            onResult(product)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}", e)
        }
    }

    suspend fun getAllProducts(onResult: suspend (List<Product>) -> Unit){
        try {
            val products = supabase.from(PRODUCTS).select().decodeList<Product>()
            onResult(products)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all products: ${e.message}", e)
            onResult(emptyList())
        }
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
    // ==============================
    // REALTIME LISTENER FUNCTION
    // ==============================

    fun listenToProductChanges(
        scope: CoroutineScope,
        onInsert: suspend (Product) -> Unit,
        onUpdate: suspend (Product) -> Unit,
        onDelete: suspend (Product) -> Unit,
        onProductFoundInCache: suspend (id:String)->Boolean
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val channel = supabase.channel("products-changes")

                val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = PRODUCTS
                }

                channel.subscribe()

                changeFlow.collect { action ->
                    when (action) {
                        is PostgresAction.Insert -> {
                            action.decodeRecord<Product>().let { newProduct ->
                                if(onProductFoundInCache(newProduct.id))
                                    return@collect
                                onInsert(newProduct)
                                Log.d(TAG, "Product inserted: ${newProduct.id}")
                            }
                        }
                        is PostgresAction.Update -> {
                            action.decodeRecord<Product>().let { updatedProduct ->
                                onUpdate(updatedProduct)
                                Log.d(TAG, "Product updated: ${updatedProduct.id}")
                            }
                        }
                        is PostgresAction.Delete -> {
                            action.decodeOldRecord<Product>().let { deletedProduct ->
                                onDelete(deletedProduct)
                                Log.d(TAG, "Product deleted: ${deletedProduct.id}")
                            }
                        }
                        else -> {
                            Log.d(TAG, "Unknown action type")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in realtime listener: ${e.message}", e)
            }
        }
    }


}
