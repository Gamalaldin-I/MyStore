package com.example.data.remote.repo

import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.Notification
import com.example.domain.repo.NotificationsRepo
import com.example.domain.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class NotificationsRepoImp(
    private val supa: SupabaseClient,
    private val networkHelper: NetworkHelperInterface
): NotificationsRepo {
    companion object{
        const val TAG = "NotificationsRepo"
        const val NOTIFICATIONS_COLLECTION = "notifications"
    }
    override suspend fun insertNotification(notification:Notification): Pair<Boolean, String> {
        try{
            if(!networkHelper.isConnected()) return Pair(false, Constants.NO_INTERNET_CONNECTION)
            supa.from(NOTIFICATIONS_COLLECTION).insert(notification)
            return Pair(true,"Success to push notification")
        }catch (_:Exception){
            return Pair(false,"Fail to push notification")
        }
    }

    override suspend fun getNotifications(): Pair<List<Notification>, String> {
        try{
            if(!networkHelper.isConnected()) return Pair(emptyList(), Constants.NO_INTERNET_CONNECTION)
            val notifications = supa.from(NOTIFICATIONS_COLLECTION)
                .select {
                    order(
                        column = "createdAt",
                        order = Order.DESCENDING,
                        nullsFirst = false
                    )
                }
                .decodeList<Notification>()
            return if(notifications.isEmpty()) Pair(emptyList(),"No notifications found") else {
                Pair(notifications,"Success to get notifications")
            }
            }catch (_:Exception){
            return Pair(emptyList(),"Fail to get notifications")
        }
    }
}