package com.yourfirm.autoreply.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yourfirm.autoreply.App
import com.yourfirm.autoreply.MainActivity
import com.yourfirm.autoreply.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service — keeps the process alive and prevents Android from
 * killing the [NotificationService] due to background restrictions.
 *
 * START_STICKY: if the OS kills this service, it will restart it automatically.
 *
 * On start: cleans up cooldown entries older than 48 hours to keep the DB lean.
 */
class ForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        cleanupOldCooldowns()
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val channelId = "autoreply_service"
        val channel = NotificationChannel(
            channelId,
            "Auto Reply Service",
            NotificationManager.IMPORTANCE_LOW   // Low = no sound, but persistent
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        // Tapping the notification opens the app
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.service_notif_title))
            .setContentText(getString(R.string.service_notif_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)           // Cannot be swiped away by user
            .setContentIntent(openApp)
            .build()
    }

    private fun cleanupOldCooldowns() {
        scope.launch {
            val cutoff = System.currentTimeMillis() - 48 * 3_600_000L
            (application as App).database.cooldownDao().cleanup(cutoff)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, ForegroundService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ForegroundService::class.java))
        }
    }
}
