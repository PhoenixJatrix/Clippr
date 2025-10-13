package com.nullinnix.clippr.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullinnix.clippr.misc.Notification
import com.nullinnix.clippr.misc.NotificationType
import com.nullinnix.clippr.misc.NotificationsState
import com.nullinnix.clippr.misc.epoch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

class NotificationsViewModel: ViewModel() {
    private val _notificationsState = MutableStateFlow(NotificationsState())
    val notificationsState = _notificationsState.asStateFlow()

    init {
        monitorNotifications()
    }

    fun postNotification (notification: Notification) {
        if (notificationsState.value.notifications.size < 3 || notification.type is NotificationType.DelayedOperation) {
            _notificationsState.update {
                if (notification.type is NotificationType.DelayedOperation) {
                    it.copy(notifications = it.notifications + notification.copy(startedAt = LocalDateTime.now().epoch(), duration = notification.type.delay.toLong() + 1))
                } else {
                    it.copy(notifications = it.notifications + notification.copy(startedAt = LocalDateTime.now().epoch()))
                }
            }
        } else {
            _notificationsState.update {
                it.copy(pendingNotifications = it.pendingNotifications + notification)
            }
        }
    }

    fun popNotification (notification: Notification) {
        _notificationsState.update {
            it.copy(notifications = it.notifications - notification)
        }
    }

    fun monitorNotifications () {
        viewModelScope.launch {
            while(true) {
                for (notification in notificationsState.value.notifications) {
                    if ((notification.duration + notification.startedAt) <= LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) {
                        popNotification(notification)
                    }
                }

                if (notificationsState.value.notifications.size < 3 && notificationsState.value.pendingNotifications.isNotEmpty()) {
                    val first = notificationsState.value.pendingNotifications.first()
                    _notificationsState.update {
                        it.copy(pendingNotifications = it.pendingNotifications - first)
                    }

                    postNotification(first)
                }

                delay(500)
            }
        }
    }
}