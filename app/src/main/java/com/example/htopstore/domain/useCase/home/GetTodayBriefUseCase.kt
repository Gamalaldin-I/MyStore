package com.example.htopstore.domain.useCase.home

import com.example.htopstore.data.local.repo.home.HomeRepoImp
import com.example.htopstore.domain.model.DayBrief
import com.example.htopstore.util.DateHelper
import javax.inject.Inject

class GetTodayBriefUseCase @Inject constructor(private val homeRepo: HomeRepoImp){
    suspend operator fun invoke(): DayBrief {
            val toDay = DateHelper.getCurrentDate()
            val profit = homeRepo.getProfitToday(toDay)?:0.0
            val income = homeRepo.getIncomeToday(toDay)?:0.0
            val expenses = homeRepo.getExpensesToday(toDay)?:0.0
        return DayBrief(income, expenses, profit)
        }

    }
