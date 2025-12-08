package com.example.domain.repo

import com.example.domain.model.Notification

interface NotificationsRepo {
    suspend fun insertNotification(notification:Notification):Pair<Boolean,String>
    suspend fun getNotifications():Pair<List<Notification>,String>
}