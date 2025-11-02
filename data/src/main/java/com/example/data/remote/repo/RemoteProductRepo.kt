package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.firebase.FirebaseUtils
import com.example.data.remote.supabase.SupabaseHelper
import com.example.domain.model.Product
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoteProductRepo(
    fdb: FirebaseFirestore,
    private val supa: SupabaseHelper,
    pref: SharedPref
) {
    private var listener: ListenerRegistration? = null
    private val fu = FirebaseUtils
    private val ownerId = pref.getStore().ownerId
    private val storeId = pref.getStore().id

    private val productsRef = fdb.collection(fu.OWNERS).document(ownerId)
        .collection(fu.STORES).document(storeId)
        .collection(fu.PRODUCTS)

    companion object {
        private const val ID = "id"
        private const val LAST_UPDATE = "lastUpdate"
    }

    private enum class Operation { ADD, UPDATE, DELETE }

    private val localOps = mutableMapOf<String, Operation>()

    fun addProduct(product: Product, insertToRoom: suspend (Product) -> Unit) {
        localOps[product.id] = Operation.ADD

        val productData = hashMapOf(
            ID to product.id,
            LAST_UPDATE to product.lastUpdate
        )

        productsRef.document(product.id)
            .set(productData)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        supa.addProduct(product) { insertedProduct ->
                            insertToRoom(insertedProduct)
                        }
                    } catch (e: Exception) {
                        Log.e("RemoteProductRepo", "Supabase add failed: ${e.message}")
                    } finally {
                        localOps.remove(product.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RemoteProductRepo", "Firebase add failed: ${e.message}")
                localOps.remove(product.id)
            }
    }

    fun deleteProduct(id: String, deleteFromRoom: suspend (String) -> Unit) {
        localOps[id] = Operation.DELETE

        productsRef.document(id)
            .delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        supa.deleteProduct(id) {
                            deleteFromRoom(id)
                        }
                    } catch (e: Exception) {
                        Log.e("RemoteProductRepo", "Supabase delete failed: ${e.message}")
                    } finally {
                        localOps.remove(id)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RemoteProductRepo", "Firebase delete failed: ${e.message}")
                localOps.remove(id)
            }
    }


    fun updateProduct(product: Product, updateToRoom: suspend (Product) -> Unit) {
        localOps[product.id] = Operation.UPDATE

        productsRef.document(product.id)
            .update(LAST_UPDATE, product.lastUpdate)
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        supa.updateProduct(product) {
                            updateToRoom(product)
                        }
                    } catch (e: Exception) {
                        Log.e("RemoteProductRepo", "Supabase update failed: ${e.message}")
                    } finally {
                        localOps.remove(product.id)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("RemoteProductRepo", "Firebase update failed: ${it.message}")
                localOps.remove(product.id)
            }
    }

    fun getAllProducts(onRoomStore: suspend (products: List<Product>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val products = supa.getAllProducts()
                onRoomStore(products)
            } catch (e: Exception) {
                Log.e("RemoteProductRepo", "Supabase get failed: ${e.message}")
            }
        }
    }

    fun listenToRemoteChanges(
        onAdd: suspend (product: Product) -> Unit,
        onUpdate: suspend (product: Product) -> Unit,
        onDelete: suspend (id: String) -> Unit
    ) {
        listener?.remove()

        listener = productsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("RemoteProductRepo", "Firebase listen failed: ${error.message}")
                return@addSnapshotListener
            }

            snapshot?.documentChanges?.forEach { doc ->
                val p = doc.document.toObject(FireProduct::class.java)
                val op = localOps[p.id]

                CoroutineScope(Dispatchers.IO).launch {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (op != Operation.ADD) {
                                supa.getProductById(p.id)?.let { onAdd(it) }
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (op != Operation.UPDATE) {
                                supa.getProductById(p.id)?.let { onUpdate(it) }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            onDelete(p.id)
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    data class FireProduct(
        val id: String = "",
        val lastUpdate: String = ""
    )
}
