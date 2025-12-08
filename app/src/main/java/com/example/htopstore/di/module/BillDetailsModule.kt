package com.example.htopstore.di.module

import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.remote.repo.RemoteBillRepo
import com.example.data.remote.repo.RemoteSalesRepo
import com.example.data.repo.BillDetailsRepoImp
import com.example.domain.repo.BillDetailsRepo
import com.example.domain.repo.StaffRepo
import com.example.domain.useCase.bill.DeleteBillUseCase
import com.example.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.domain.useCase.billDetails.ReturnProductUseCase
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillDetailsModule {

    @Provides
    @Singleton
    fun provideBillDetRepo(salesDao: SalesDao,productDao: ProductDao,remoteS: RemoteSalesRepo,remoteB: RemoteBillRepo): BillDetailsRepo {
        return BillDetailsRepoImp(salesDao,productDao,remoteB,remoteS)
    }

    @Provides
    fun provideGetBillDetailsUseCse(localRpo: BillDetailsRepo): GetBillDetailsUseCse {
        return GetBillDetailsUseCse(localRpo)
    }

    @Provides
    fun provideInsertReturnProduct(localRpo: BillDetailsRepo,staffRepo: StaffRepo,
                                   insNot: InsertNotificationUseCase): ReturnProductUseCase {
        return ReturnProductUseCase(localRpo,staffRepo,insNot)
    }

    @Provides
    fun provideDeleteBillUseCase(localRpo: BillDetailsRepo): DeleteBillUseCase {
        return DeleteBillUseCase(localRpo)
    }

}