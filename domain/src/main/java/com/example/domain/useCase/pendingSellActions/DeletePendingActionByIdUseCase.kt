package com.example.domain.useCase.pendingSellActions

import com.example.domain.repo.SalesRepo

class DeletePendingActionByIdUseCase(private val repo: SalesRepo){
    suspend operator fun invoke(id: Int) {
        repo.deletePendingActionById(id)
    }
}