package com.example.domain.useCase.notifications

import com.example.domain.model.Notification
import com.example.domain.repo.NotificationsRepo


class InsertNotificationUseCase(private val repo: NotificationsRepo) {
    suspend operator fun invoke(notification: Notification) = repo.insertNotification(notification = notification)
}