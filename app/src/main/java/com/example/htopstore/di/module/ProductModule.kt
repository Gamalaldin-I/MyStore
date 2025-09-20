package com.example.htopstore.di.module

import com.example.domain.repo.ProductRepo
import com.example.domain.useCase.product.AddProductUseCase
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.product.GetLowStockUseCase
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.useCase.product.GetTop5UseCase
import com.example.domain.useCase.product.UpdateProductUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ProductModule {

    //operation on product
    @Provides
    fun provideAddProductUseCase(productRepo: ProductRepo): AddProductUseCase {
        return AddProductUseCase(productRepo)
    }
    @Provides
    fun provideDeleteProductUseCase(productRepo: ProductRepo): DeleteProductUseCase {
        return DeleteProductUseCase(productRepo)
    }
    @Provides
    fun provideUpdateProductUseCase(productRepo: ProductRepo): UpdateProductUseCase {
        return UpdateProductUseCase(productRepo)
    }
    @Provides
    fun provideGetProductUseCase(productRepo: ProductRepo): GetProductByIdUseCase {
        return GetProductByIdUseCase(productRepo)
    }


    //Queries
    @Provides
    fun provideGetTop5InSalesUseCase(productRepo: ProductRepo): GetTop5UseCase {
        return GetTop5UseCase(productRepo)
    }
    @Provides
    fun provideGetLowStockUseCase(productRepo: ProductRepo): GetLowStockUseCase {
        return GetLowStockUseCase(productRepo)
    }
    @Provides
    fun provideGetAvailableProductsUseCase(productRepo: ProductRepo): GetAvailableProductsUseCase {
        return GetAvailableProductsUseCase(productRepo)
    }
    @Provides
    fun provideGetArchiveProductsUseCase(productRepo: ProductRepo): GetArchiveProductsUseCase {
        return GetArchiveProductsUseCase(productRepo)
    }

}