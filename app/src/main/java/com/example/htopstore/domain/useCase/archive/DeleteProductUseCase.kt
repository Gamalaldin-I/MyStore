package com.example.htopstore.domain.useCase.archive

import android.media.Image
import com.example.htopstore.data.local.repo.archieve.ArchiveRepoImp
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(private val productRepository: ArchiveRepoImp) {
    suspend operator fun invoke(productId: String,imageUri: String){
        productRepository.deleteProductFromArchive(productId,imageUri)
    }
}