package com.example.domain.useCase.pendingSellActions

import com.example.domain.repo.SalesRepo

class DeleteApprovedSellActionsUseCase(private val repo: SalesRepo) {
    suspend operator fun invoke() {
        repo.deleteApprovedSellAction()
    }
}