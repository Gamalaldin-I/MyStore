package com.example.domain.useCase.pendingSellActions

import com.example.domain.model.PendingSellAction
import com.example.domain.repo.SalesRepo

class AddSellPendingActionUseCase(private val repo: SalesRepo) {
    suspend operator fun invoke(pending: PendingSellAction){
        repo.insertPendingSellAction(pending)
    }
}