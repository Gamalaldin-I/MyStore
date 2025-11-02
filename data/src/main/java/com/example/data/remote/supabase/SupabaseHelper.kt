package com.example.data.remote.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.domain.model.Product
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.io.File

class SupabaseHelper(private val supabase: SupabaseClient,private val context: Context) {
    companion object{
        private const val PRODUCT_BUCKET = "Products"
        private const val AVATARS_BUCKET = "Avatars"
        private const val PRODUCTS = "products"
    }


    private suspend fun uploadImage(uri: Uri,path:String,fileName: String): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return null

        val bucket = supabase.storage.from(path)

        bucket.upload(fileName, bytes)

        val publicUrl = "https://ayoanqjzciolnahljauc.supabase.co/storage/v1/object/public/$path/$fileName"
        return publicUrl
    }
    suspend fun addProduct(product: Product){
        try{
            val fileName = "${product.id}.jpg"
            val uri = Uri.fromFile(File(product.productImage))
            val url = uploadImage(
                uri = uri,
                path = PRODUCT_BUCKET,
                fileName = fileName
            )
            product.productImage = url.toString()
        supabase.from(PRODUCTS).insert(product)
        }
        catch (e:Exception){
              Log.d("SUPABASE_ERROR","${e.message}")
        }

    }

}