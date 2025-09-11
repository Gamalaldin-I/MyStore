package com.example.htopstore.data.local.repo.archieve

import android.content.Context
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.roomDb.AppDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ArchiveRepoImp(context: Context): ArchiveRepo  {
    private val archiveDao = AppDataBase.getDatabase(context).productDao()
    override suspend fun getArchiveProducts(): List<Product> =
        withContext(Dispatchers.IO) {
            archiveDao.getProductsNotAvailable()
        }
    override suspend fun deleteProductFromArchive(productId: String, image: String) = withContext(Dispatchers.IO) {
        archiveDao.deleteProductById(productId)
        val file = File(image)
        if (file.exists()){
            file.delete()
        }
    }
}