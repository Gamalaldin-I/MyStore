package com.example.data.remote.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class SupabaseHelper(private val supabase: SupabaseClient,private val context: Context) {
    companion object{
        private const val AVATARS_BUCKET = "Avatars"


        //for products
        private const val PRODUCT_BUCKET = "Products"
        private const val PRODUCTS = "products"
        private const val ID = "id"
    }


    private suspend fun uploadImage(uri: Uri,path:String,fileName: String): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return null

        val bucket = supabase.storage.from(path)

        bucket.upload(fileName, bytes)

        val publicUrl = "https://ayoanqjzciolnahljauc.supabase.co/storage/v1/object/public/$path/$fileName"
        return publicUrl
    }

    //for product
    suspend fun addProduct(product: Product,onResult:suspend (Product)->Unit){
        try{
            val fileName = "${product.id}.jpg"
            val uri = product.productImage.toUri()
            val url = uploadImage(
                uri = uri,
                path = PRODUCT_BUCKET,
                fileName = fileName
            )
            product.productImage = url.toString()
            supabase.from(PRODUCTS).insert(product)
            onResult(product)
        }
        catch (e:Exception){
              Log.d("SUPABASE_ERROR"," adding product: ${e.message}")
        }

    }
    suspend fun deleteProduct(id: String,onResult:suspend (id:String) -> Unit) {
        try {
            supabase
            .from(PRODUCTS)
            .delete{
                filter {
                    eq(ID,id)
                }
            }
            onResult(id)

        }catch (e: Exception){
            Log.d("SUPABASE_ERROR"," deleting product: ${e.message}")
        }
    }
    suspend fun updateProduct(product: Product,onResult:suspend (Product)->Unit){
        try{
            supabase.from(PRODUCTS).update(product){
                filter {
                    eq(ID,product.id)
                }
            }
            onResult(product)
        }
        catch (e:Exception){
            Log.d("SUPABASE_ERROR"," updating product: ${e.message}")

        }
    }
    suspend fun getAllProducts():List<Product>{
        return try {
            val products = supabase.from(PRODUCTS).select().decodeList<Product>()
            products
        }
        catch (e:Exception){
            Log.d("SUPABASE_ERROR"," getting all products: ${e.message}")
            emptyList()
        }
    }
    suspend fun getProductById(id:String): Product?{
        return try {
            val product = supabase.from(PRODUCTS).select{
                filter {
                    eq(ID,id)
                }
            }.decodeSingle<Product>()
            product
            }
        catch (e:Exception){
            Log.d("SUPABASE_ERROR"," getting product by id: ${e.message}")
            null
        }
    }


}