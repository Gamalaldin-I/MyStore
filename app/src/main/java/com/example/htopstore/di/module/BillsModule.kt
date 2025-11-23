package com.example.htopstore.di.module

import com.example.data.local.dao.SalesDao
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.RemoteBillRepo
import com.example.data.repo.BillRepoImp
import com.example.domain.repo.BillRepo
import com.example.domain.repo.SalesRepo
import com.example.domain.useCase.bill.FetchAllNewBillsUseCase
import com.example.domain.useCase.bill.GetAllBillsUseCase
import com.example.domain.useCase.bill.GetBillByDateUseCase
import com.example.domain.useCase.bill.GetBillsByDateRangeUseCase
import com.example.domain.useCase.bill.GetBillsTillDateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillsModule {


    @Provides
    fun provideRemoteBillRepo(supaBase: SupabaseClient,
                              netWorkHelper: NetworkHelperInterface,
                              pref: SharedPref): RemoteBillRepo {
        return RemoteBillRepo(
            supabase = supaBase,
            networkManager = netWorkHelper,
            pref = pref
        )
    }


    @Provides
    fun provideBillRepo(salesDao: SalesDao,remote: RemoteBillRepo): BillRepo {
        return BillRepoImp(salesDao,remote)
    }

    @Provides
    @Singleton
    fun provideGetAllBillsUseCase(billRepo: BillRepo): GetAllBillsUseCase {
        return GetAllBillsUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsByDateUseCase(billRepo: BillRepo): GetBillByDateUseCase {
        return GetBillByDateUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsByDateRangeUseCase(billRepo: BillRepo): GetBillsByDateRangeUseCase {
        return GetBillsByDateRangeUseCase(billRepo)
    }

    @Provides
    @Singleton
    fun provideGetBillsTillDateUseCase(billRepo: BillRepo): GetBillsTillDateUseCase {
        return GetBillsTillDateUseCase(billRepo)
    }
    @Provides
    @Singleton
    fun provideFetchAllNewBillsUseCase(billRepo: BillRepo,salesRepo: SalesRepo): FetchAllNewBillsUseCase{
        return FetchAllNewBillsUseCase(billRepo,salesRepo)
    }
}