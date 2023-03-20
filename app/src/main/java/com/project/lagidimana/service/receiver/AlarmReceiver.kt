package com.project.lagidimana.service.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.project.lagidimana.Const.CHANNEL_ID
import com.project.lagidimana.Const.CLEAR_DATA_NOTIFICATION_ID
import com.project.lagidimana.R
import com.project.lagidimana.data.AppRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver: BroadcastReceiver() {
    private val helper: AlarmReceiverHelper by lazy { AlarmReceiverHelper() }

    override fun onReceive(context: Context, intent: Intent?) {
        helper.onReceive(context)
    }
}

class AlarmReceiverHelper: KoinComponent{
    private val appRepository: AppRepository by inject()

    fun onReceive(context: Context) {
        appRepository.deleteAll()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_delete_sweep)
            .setContentTitle(context.getString(R.string.app_notification_clear_data_title))
            .setContentText(context.getString(R.string.app_notification_clear_data_message))
            .setPriority(NotificationCompat.PRIORITY_MAX)

        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(
            CLEAR_DATA_NOTIFICATION_ID, builder.build()
        )
    }
}