package com.example.htopstore.di

import android.content.Context
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import com.example.htopstore.domain.useCase.billDetails.DeleteBillUseCase
import com.example.htopstore.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.htopstore.domain.useCase.billDetails.InsertReturnProduct
import com.example.htopstore.domain.useCase.billDetails.UpdateProductQuantityAfterReturn
import com.example.htopstore.domain.useCase.billDetails.UpdateSoldProductQuantityAfterReturn
import com.example.htopstore.domain.useCase.billDetails.UpdateTotalCashOfBillAfterReturn
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BillDetailsModule {
    @Provides
    fun provideBillRepo( @ApplicationContext context: Context): BillDetailsRepoImp {
        return BillDetailsRepoImp(context)
    }
    @Provides
    fun provideGetBillDetailsUseCse(localRpo: BillDetailsRepoImp): GetBillDetailsUseCse {
        return GetBillDetailsUseCse(localRpo)
    }
    @Provides
    fun provideUpdateTotalCashOfBillAfterReturn(localRpo: BillDetailsRepoImp): UpdateTotalCashOfBillAfterReturn {
        return UpdateTotalCashOfBillAfterReturn(localRpo)
    }
    @Provides
    fun provideUpdateProductQuantityAfterReturn(localRpo: BillDetailsRepoImp): UpdateProductQuantityAfterReturn {
        return UpdateProductQuantityAfterReturn(localRpo)
    }
    @Provides
    fun provideInsertReturnProduct(localRpo: BillDetailsRepoImp): InsertReturnProduct {
        return InsertReturnProduct(localRpo)
    }
    @Provides
    fun provideUpdateSoldProductQuantityAfterReturn(localRpo: BillDetailsRepoImp): UpdateSoldProductQuantityAfterReturn {
        return UpdateSoldProductQuantityAfterReturn(localRpo)
    }
    @Provides
    fun provideDeleteBillUseCase(localRpo: BillDetailsRepoImp): DeleteBillUseCase {
        return DeleteBillUseCase(localRpo)
    }

}