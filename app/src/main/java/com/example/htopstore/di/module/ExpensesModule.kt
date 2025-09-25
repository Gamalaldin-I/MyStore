package com.example.htopstore.di.module

import com.example.domain.repo.ExpensesRepo
import com.example.domain.useCase.expenses.GetAllExpensesUseCase
import com.example.domain.useCase.expenses.GetExpensesByDateUseCase
import com.example.domain.useCase.expenses.InsertNewExpenseUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpensesModule {

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

}