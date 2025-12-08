package com.example.htopstore.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Notification
import com.example.domain.useCase.notifications.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun getNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            try {
                // Add minimum loading time for better UX
                val startTime = System.currentTimeMillis()
                val result = getNotificationsUseCase()

                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 500) {
                    delay(500 - elapsed)
                }

                _notifications.postValue(result.first)
                _message.postValue(result.second)
            } catch (e: Exception) {
                _notifications.postValue(emptyList())
                _message.postValue(e.message ?: "Failed to load notifications")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Implement mark as read functionality
            val updatedList = _notifications.value?.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(deleted = true)
                } else {
                    notification
                }
            }
            _notifications.postValue(updatedList ?: emptyList())
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Implement delete functionality
            val updatedList = _notifications.value?.filter { it.id != notificationId }
            _notifications.postValue(updatedList ?: emptyList())
        }
    }
}