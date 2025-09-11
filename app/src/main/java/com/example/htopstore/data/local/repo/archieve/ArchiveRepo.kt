package com.example.htopstore.data.local.repo.archieve

import com.example.htopstore.data.local.model.Product

interface ArchiveRepo {
    suspend fun getArchiveProducts(): List<Product>
    suspend fun deleteProductFromArchive(productId: String,image: String)
}