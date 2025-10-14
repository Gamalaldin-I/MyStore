package com.example.htopstore.di.module.analysis

import com.example.domain.repo.AnalysisRepo
import com.example.domain.useCase.analisys.sales.GetExpensesWithCategoryUseCase
import com.example.domain.useCase.analisys.sales.GetProfitByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetSalesAndProfitByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheAVGSalesUseCase
import com.example.domain.useCase.analisys.sales.GetTheMostSellingDayByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheMostSellingHourByPeriodUseCase
import com.example.domain.useCase.analisys.sales.GetTheNumOfSalesOpsUseCase
import com.example.domain.useCase.analisys.sales.GetTheTotalSalesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object SalesAnalysisModule {
    @Provides
    fun provideSalesAndProfitByPeriodUseCase(analysisRepo: AnalysisRepo): GetSalesAndProfitByPeriodUseCase {
        return GetSalesAndProfitByPeriodUseCase(analysisRepo)
    }

    @Provides
    fun provideAvgOfSaleOpUseCase(repo: AnalysisRepo): GetTheAVGSalesUseCase{
        return GetTheAVGSalesUseCase(repo)
    }
    @Provides
    fun provideNumberOfSalesUseCase(repo: AnalysisRepo): GetTheNumOfSalesOpsUseCase{
        return GetTheNumOfSalesOpsUseCase(repo)
    }
    @Provides
    fun provideTotalOfSalesByPeriodUseCase(repo: AnalysisRepo): GetTheTotalSalesUseCase{
        return GetTheTotalSalesUseCase(repo)
    }
    @Provides
    fun provideBestPeriodOfSalesUseCase(repo: AnalysisRepo): GetTheMostSellingHourByPeriodUseCase{
        return GetTheMostSellingHourByPeriodUseCase(repo)
    }
    @Provides
    fun provideBestDayOfWeekUseCase(repo: AnalysisRepo): GetTheMostSellingDayByPeriodUseCase{
        return GetTheMostSellingDayByPeriodUseCase(repo)
    }
    @Provides
    fun provideProfitUseCase(repo: AnalysisRepo): GetProfitByPeriodUseCase{
        return GetProfitByPeriodUseCase(repo)
    }
    // for expenses
    @Provides
    fun provideExpensesByCategoryUseCase(repo: AnalysisRepo): GetExpensesWithCategoryUseCase{
        return GetExpensesWithCategoryUseCase(repo)
    }
}