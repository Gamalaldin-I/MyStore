package com.example.data.remote.repo

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.firebase.FirebaseUtils
import com.example.data.remote.supabase.SupabaseHelper
import com.example.domain.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RemoteProductRepo(
    private val fdb: FirebaseFirestore,
    private val supa: SupabaseHelper,
    private val pref: SharedPref){
    val fu = FirebaseUtils
    companion object{
        private const val ID = "id"
        private const val NAME = "name"
        private const val BUYING_PRICE = "buyingPrice"
        private const val SELLING_PRICE = "sellingPrice"
        private const val COUNT = "count"
        private const val CATEGORY = "category"
        private const val IMAGE_URL = "imageURL"
        private const val ADDING_DATE = "addingDate"
        private const val SOLD_COUNT = "soldCount"
        private const val LAST_UPDATE = "lastUpdate"

    }
    suspend fun addProduct(product: Product){
        try {
            val ownerId = pref.getUser().id
            val storeId = pref.getStore().id

            val productData = hashMapOf(
                ID to product.id,
                LAST_UPDATE to product.lastUpdate
            )

            withContext(Dispatchers.IO) {
                fdb.collection(fu.OWNERS)
                    .document(ownerId)
                    .collection(fu.STORES)
                    .document(storeId)
                    .collection(fu.PRODUCTS)
                    .document(product.id)
                    .set(productData)
                    .await()
            }
            // add to supabase
            supa.addProduct(product)
        } catch (e: Exception) {
        }
    }




}



