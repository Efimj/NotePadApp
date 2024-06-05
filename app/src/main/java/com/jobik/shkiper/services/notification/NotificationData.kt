package com.jobik.shkiper.services.notification

import androidx.annotation.Keep
import com.jobik.shkiper.database.models.RepeatMode

@Keep
data class NotificationData(
    val noteId: String,
    val notificationId: Int,
    val title: String,
    val message: String,
    val repeatMode: RepeatMode,
    val requestCode: Int,
    val trigger: Long,
    val channel: NotificationScheduler.Companion.NotificationChannels = NotificationScheduler.Companion.NotificationChannels.NOTECHANNEL
)
