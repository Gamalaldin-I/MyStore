package com.example.htopstore.di.module

import android.content.Context
import com.example.data.local.dao.ProductDao
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.RemoteProductRepo
import com.example.data.repo.ProductRepoImp
import com.example.domain.repo.ProductRepo
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.useCase.product.AddPendingProductsUseCase
import com.example.domain.useCase.product.AddProductUseCase
import com.example.domain.useCase.product.DeletePendingProductUseCase
import com.example.domain.useCase.product.DeleteProductUseCase
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetArchiveSizeUseCase
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import com.example.domain.useCase.product.GetPendingProductsYUseCase
import com.example.domain.useCase.product.GetProductByIdUseCase
import com.example.domain.useCase.product.UpdateProductImageUseCase
import com.example.domain.useCase.product.UpdateProductUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProductModule {

    //operation on product
    @Provides
    @Singleton
    fun provideProductRepo(productDao: ProductDao,
                           remote: RemoteProductRepo,
                           ): ProductRepo {
        return ProductRepoImp(
            productDao= productDao,
            remote= remote,
            )
    }
    @Provides
    @Singleton
    fun provideRemoteProductRepo(
        @ApplicationContext context: Context,
        supabase: SupabaseClient,
        pref: SharedPref,
        networkHelper: NetworkHelperInterface,
        insertNotificationUseCase: InsertNotificationUseCase
    ):RemoteProductRepo{
        return RemoteProductRepo(
            supabase = supabase,
            pref = pref,
            context = context,
            networkHelper = networkHelper,
            notSender = insertNotificationUseCase
        )
    }



    @Provides
    fun provideAddProductUseCase(productRepo: ProductRepo,staffRepo: StaffRepo): AddProductUseCase {
        return AddProductUseCase(productRepo,staffRepo)
    }
    @Provides
    fun provideDeleteProductUseCase(productRepo: ProductRepo,staffRepo: StaffRepo): DeleteProductUseCase {
        return DeleteProductUseCase(productRepo,staffRepo)
    }
    @Provides
    fun provideDeletePendingProductUseCase(productRepo: ProductRepo): DeletePendingProductUseCase {
        return DeletePendingProductUseCase(productRepo)
    }
    @Provides
    fun provideUpdateProductUseCase(productRepo: ProductRepo,staffRepo: StaffRepo): UpdateProductUseCase {
        return UpdateProductUseCase(productRepo,staffRepo)
    }
    @Provides
    fun provideGetProductUseCase(productRepo: ProductRepo): GetProductByIdUseCase {
        return GetProductByIdUseCase(productRepo)
    }

    //Queries
    @Provides
    fun provideGetAvailableProductsUseCase(productRepo: ProductRepo): GetAvailableProductsUseCase {
        return GetAvailableProductsUseCase(productRepo)
    }
    @Provides
    fun provideGetArchiveProductsUseCase(productRepo: ProductRepo): GetArchiveProductsUseCase {
        return GetArchiveProductsUseCase(productRepo)
    }
    @Provides
    fun provideGetArchiveSizeUseCase(productRepo: ProductRepo): GetArchiveSizeUseCase {
        return GetArchiveSizeUseCase(productRepo)
    }
    @Provides
    fun uploadProductsUseCase(productRepo: ProductRepo): AddPendingProductsUseCase{
        return AddPendingProductsUseCase(productRepo)
    }
    @Provides
    fun provideGetPendingProductsYUseCase(productRepo: ProductRepo): GetPendingProductsYUseCase{
        return GetPendingProductsYUseCase(productRepo)
    }
    @Provides
    fun provideUpdateProductImageUseCase(productRepo: ProductRepo): UpdateProductImageUseCase{
        return UpdateProductImageUseCase(productRepo)
    }

}