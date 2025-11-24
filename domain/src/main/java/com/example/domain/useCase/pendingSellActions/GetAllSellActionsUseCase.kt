package com.example.domain.useCase.pendingSellActions

import com.example.domain.model.PendingSellAction
import com.example.domain.repo.SalesRepo
import kotlinx.coroutines.flow.Flow

class GetAllSellActionsUseCase(private val repo: SalesRepo) {
     operator fun invoke(): Flow<List<PendingSellAction>> =
         repo.getAllPendingAndApproved()

}