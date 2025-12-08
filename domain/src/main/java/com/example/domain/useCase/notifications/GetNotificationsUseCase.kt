package com.example.domain.useCase.notifications

import com.example.domain.repo.NotificationsRepo

class GetNotificationsUseCase(private val repo: NotificationsRepo) {
    suspend operator fun invoke() = repo.getNotifications()
}