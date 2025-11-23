package com.example.htopstore.di.module

import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.data.remote.repo.RemoteSalesRepo
import com.example.domain.repo.BillRepo
import com.example.domain.repo.SalesRepo
import com.example.domain.useCase.sales.GetAllSalesAndReturnsByDateUseCase
import com.example.domain.useCase.sales.GetAllSalesAndReturnsUseCase
import com.example.domain.useCase.sales.GetReturnsByDateUseCase
import com.example.domain.useCase.sales.GetReturnsUseCase
import com.example.domain.useCase.sales.GetSoldOnlyByDateUseCase
import com.example.domain.useCase.sales.GetSoldOnlyUseCase
import com.example.domain.useCase.sales.GetTheTotalOfTheProfitByRangeOfDaysUseCase
import com.example.domain.useCase.sales.GetTotalOfSalesByRageOfDaysUseCase
import com.example.domain.useCase.sales.SellUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SalesModule {
    @Provides
    @Singleton
    fun provideRemoteSalesRepo(supaBase: SupabaseClient, netWorkHelper: NetworkHelperInterface,pref: SharedPref): RemoteSalesRepo {
        return RemoteSalesRepo(supabase = supaBase, pref = pref, networkManager = netWorkHelper)
    }


    @Provides
    fun provideSellUseCase(salesRepo: SalesRepo,billRepo: BillRepo): SellUseCase {
        return SellUseCase(salesRepo,billRepo)
    }
    @Provides
    fun provideGetAllSalesAndReturnsUseCase(salesRepo: SalesRepo): GetAllSalesAndReturnsUseCase {
        return GetAllSalesAndReturnsUseCase(salesRepo)
    }

    @Provides
    fun provideGetAllSalesAndReturnsByDateUseCase(salesRepo:SalesRepo): GetAllSalesAndReturnsByDateUseCase {
        return GetAllSalesAndReturnsByDateUseCase(salesRepo)
    }

    @Provides
    fun provideGetSoldOnlyUseCase(salesRepo:SalesRepo): GetSoldOnlyUseCase {
        return GetSoldOnlyUseCase(salesRepo)
    }

    @Provides
    fun provideGetSoldOnlyByDateUseCase(salesRepo:SalesRepo): GetSoldOnlyByDateUseCase {
        return GetSoldOnlyByDateUseCase(salesRepo)
    }

    @Provides

    fun provideGetReturnsUseCase(salesRepo: SalesRepo): GetReturnsUseCase {
        return GetReturnsUseCase(salesRepo)
    }

    @Provides
    fun provideGetReturnsByDateUseCase(salesRepo:SalesRepo): GetReturnsByDateUseCase {
        return GetReturnsByDateUseCase(salesRepo)
    }
    @Provides
    fun provideGetTotalOfSalesUseCase(salesRepo: SalesRepo): GetTotalOfSalesByRageOfDaysUseCase{
        return GetTotalOfSalesByRageOfDaysUseCase(salesRepo)
    }
    @Provides
    fun provideGetTotalOfProfitUseCase(salesRepo: SalesRepo): GetTheTotalOfTheProfitByRangeOfDaysUseCase{
        return GetTheTotalOfTheProfitByRangeOfDaysUseCase(salesRepo)
    }
}