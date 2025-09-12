package com.example.htopstore.domain.useCase.home
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.data.local.repo.home.HomeRepoImp
import javax.inject.Inject

class GetLowStockUseCase @Inject constructor (private val homeRepo: HomeRepoImp){
    suspend operator fun invoke(): List<Product>  =
        homeRepo.getLowStock() as MutableList<Product>

}