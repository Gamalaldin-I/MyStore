package com.example.domain.useCase.pendingSellActions

import com.example.domain.model.PendingSellAction
import com.example.domain.repo.SalesRepo

class GetSellPendingActionByIdUseCase(private val repo: SalesRepo){
    suspend operator fun invoke(id: Int): PendingSellAction {
        return repo.getPendingActionById(id)
    }
}