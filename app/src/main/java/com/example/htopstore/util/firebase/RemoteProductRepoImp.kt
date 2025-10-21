package com.example.htopstore.util.firebase

import android.util.Log
import com.example.data.remote.RemoteProductRepo
import com.example.domain.model.Product
import com.google.firebase.firestore.FirebaseFirestore

class RemoteProductRepoImp(val db: FirebaseFirestore): RemoteProductRepo {
    val fu = FirebaseUtils
    override fun addListOfProducts(products: List<Product>) {
        TODO("Not yet implemented")
    }

    override fun addProduct(product: Product) {

        val remoteProduct = hashMapOf(
            fu.PRODUCT_NAME to product.name,
            fu.PRODUCT_BUYING_PRICE to product.buyingPrice,
            fu.PRODUCT_COUNT to product.count,
            fu.PRODUCT_ADDING_DATE to product.addingDate,
            fu.PRODUCT_CATEGORY to product.category,
            fu.PRODUCT_IMAGE_URL to product.productImage.toString(),
            fu.PRODUCT_SELLING_PRICE to product.sellingPrice,
            fu.PRODUCT_SOLD_COUNT to product.soldCount,
        )

        db.collection(fu.STORES).document(fu.STORE_ID)
            .collection(fu.PRODUCTS).document(product.id).set(
                remoteProduct
            ).addOnSuccessListener {
                Log.d("Firebase","Successsssssssss{${product.id}}")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "فشل: ", e)
            }
    }

    override fun updateProduct(product: Product) {
        addProduct(product)
    }

    override fun deleteProductById(id: String) {
        TODO("Not yet implemented")
    }

    override fun getProducts(): List<Product> {
        TODO("Not yet implemented")
    }
}