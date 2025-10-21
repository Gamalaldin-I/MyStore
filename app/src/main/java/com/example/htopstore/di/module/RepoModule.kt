package com.example.htopstore.di.module

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.SalesDao
import com.example.data.remote.RemoteProductRepo
import com.example.data.repo.AnalysisRepoImp
import com.example.data.repo.BillDetailsRepoImp
import com.example.data.repo.BillRepoImp
import com.example.data.repo.ExpenseRepoImp
import com.example.data.repo.ProductRepoImp
import com.example.data.repo.SalesRepoImp
import com.example.domain.repo.AnalysisRepo
import com.example.domain.repo.BillDetailsRepo
import com.example.domain.repo.BillRepo
import com.example.domain.repo.ExpensesRepo
import com.example.domain.repo.ProductRepo
import com.example.domain.repo.SalesRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides
    fun provideProductRepo(productDao: ProductDao, remote: RemoteProductRepo): ProductRepo {
        return ProductRepoImp(productDao,remote)
    }

    @Provides
    fun provideSalesRepo(productDao: ProductDao,salesDao: SalesDao): SalesRepo {
        return SalesRepoImp(salesDao,productDao)
    }

    @Provides
    fun providesExpensesRepo(expenseDao: ExpenseDao): ExpensesRepo {
        return ExpenseRepoImp(expenseDao)
    }
    @Provides
    fun provideBillRepo(salesDao: SalesDao): BillRepo {
        return BillRepoImp(salesDao)
    }

    @Provides
    fun provideBillDetRepo(salesDao: SalesDao,productDao: ProductDao,expenseDao: ExpenseDao): BillDetailsRepo {
        return BillDetailsRepoImp(salesDao,productDao,expenseDao)
    }
    @Provides
    fun provideAnalysisRepo(expenseDao: ExpenseDao,productDao: ProductDao,salesDao: SalesDao): AnalysisRepo {
        return AnalysisRepoImp(productDao = productDao, expenseDao = expenseDao, salesDao = salesDao)
    }


}