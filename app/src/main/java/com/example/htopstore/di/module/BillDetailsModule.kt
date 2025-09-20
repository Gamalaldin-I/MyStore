package com.example.htopstore.di.module

import com.example.domain.repo.BillDetailsRepo
import com.example.domain.useCase.bill.DeleteBillUseCase
import com.example.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.domain.useCase.billDetails.ReturnProductUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BillDetailsModule {

    @Provides
    fun provideGetBillDetailsUseCse(localRpo: BillDetailsRepo): GetBillDetailsUseCse {
        return GetBillDetailsUseCse(localRpo)
    }

    @Provides
    fun provideInsertReturnProduct(localRpo: BillDetailsRepo): ReturnProductUseCase {
        return ReturnProductUseCase(localRpo)
    }

    @Provides
    fun provideDeleteBillUseCase(localRpo: BillDetailsRepo): DeleteBillUseCase {
        return DeleteBillUseCase(localRpo)
    }

}