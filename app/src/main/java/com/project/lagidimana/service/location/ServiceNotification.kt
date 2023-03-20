/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package com.project.lagidimana.service.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.VISIBILITY_PRIVATE
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN

class ServiceNotification @JvmOverloads constructor(
    context: Context,
    runningInBackground: Boolean = false
) {
    private var notificationBuilder: Builder? = null
    var notification: Notification? = null
    private var notificationPendingIntent: PendingIntent? = null
    private var notificationManager: NotificationManager? = null

    companion object {
        private val TAG : String = Notification::class.java.simpleName
        private val CHANNEL_ID = TAG
        var notificationIcon = 0
        var notificationTitle: String? = null
    }

    init {
        notification = if (runningInBackground) {
            setNotification(context)
        } else {
            val notificationTitle =
                notificationTitle
            val notificationIcon =
                notificationIcon
            setNotification(context, notificationTitle, notificationIcon)
        }
    }

    private fun setNotification(context: Context, title: String?, icon: Int): Notification {
        val notification: Notification
        notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Permanent ServiceNotification"
            val importance: Int = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val description = "I would like to receive travel alerts and notifications for:"
            channel.description = description
            notificationBuilder = Builder(context, CHANNEL_ID)
            if (notificationManager != null) {
                notificationManager!!.createNotificationChannel(channel)
            }
            notification =
                notificationBuilder!!
                    .setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentIntent(notificationPendingIntent)
                    .setVisibility(VISIBILITY_PRIVATE)
                    .build()
        } else {
            notification = Builder(
                context,
                "channelID"
            )
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setPriority(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) NotificationManager.IMPORTANCE_MIN
                    else IMPORTANCE_MIN
                )
                .setContentIntent(notificationPendingIntent)
                .setVisibility(VISIBILITY_PRIVATE)
                .build()
        }
        return notification
    }

    private fun setNotification(context: Context): Notification {
        return setNotification(context, notificationTitle, notificationIcon)
    }
}