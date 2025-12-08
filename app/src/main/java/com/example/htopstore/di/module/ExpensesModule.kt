package com.example.htopstore.di.module

import com.example.data.local.dao.ExpenseDao
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.repo.RemoteExpensesRepo
import com.example.data.repo.ExpenseRepoImp
import com.example.domain.repo.ExpensesRepo
import com.example.domain.useCase.expenses.DeleteOutcomeUseCase
import com.example.domain.useCase.expenses.FetchAllOutComesUseCase
import com.example.domain.useCase.expenses.GetAllExpensesUseCase
import com.example.domain.useCase.expenses.GetExpensesByDateUseCase
import com.example.domain.useCase.expenses.GetTotalOfExpensesByRangeOfDateUseCase
import com.example.domain.useCase.expenses.InsertNewExpenseUseCase
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpensesModule {
    @Provides
    @Singleton
    fun provideRemoteExpensesRepo(supabaseClient: SupabaseClient,
                                  insertNotificationUseCase: InsertNotificationUseCase,
                                  pref: SharedPref): RemoteExpensesRepo {
        return RemoteExpensesRepo(
            supabase =supabaseClient,
            pref = pref,
            notiSenter = insertNotificationUseCase
        )
    }
    @Provides
    @Singleton
    fun providesExpensesRepo(expenseDao: ExpenseDao,remote: RemoteExpensesRepo): ExpensesRepo {
        return ExpenseRepoImp(expenseDao,remote)
    }



    @Provides
    @Singleton
    fun provideFetchExpensesUseCase(expensesRepo: ExpensesRepo): FetchAllOutComesUseCase{
        return FetchAllOutComesUseCase(expensesRepo)
    }

    @Provides
    @Singleton
    fun provideGetAllExpensesUseCase(expensesRepo: ExpensesRepo): GetAllExpensesUseCase {
        return GetAllExpensesUseCase(expensesRepo)
    }

    @Provides
    @Singleton
    fun provideAddExpenseUseCase(expensesRepo: ExpensesRepo): InsertNewExpenseUseCase {
        return InsertNewExpenseUseCase(expensesRepo)
    }
    @Provides
    @Singleton
    fun provideGetExpensesByDateUseCase(expensesRepo: ExpensesRepo): GetExpensesByDateUseCase {
        return GetExpensesByDateUseCase(expensesRepo)
    }
    @Provides
    fun provideGetExpensesByRange(expensesRepo: ExpensesRepo): GetTotalOfExpensesByRangeOfDateUseCase{
        return GetTotalOfExpensesByRangeOfDateUseCase(expensesRepo)
    }
    @Provides
    fun provideDeleteOutcomeUseCase(repo: ExpensesRepo): DeleteOutcomeUseCase{
        return DeleteOutcomeUseCase(repo)
    }

}