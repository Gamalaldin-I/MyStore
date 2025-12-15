package com.example.domain.useCase.product

import android.net.Uri
import com.example.domain.repo.ProductRepo

class UpdateProductImageUseCase(private val repo: ProductRepo) {
    suspend operator fun invoke(uri: Uri,productId:String): Pair<Boolean, String> {
        return repo.updateProductImage(uri,productId)

}
}