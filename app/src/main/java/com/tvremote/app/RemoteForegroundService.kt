package com.tvremote.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RemoteForegroundService : Service() {
    private var server: TvHttpServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())

        if (server == null) {
            server = TvHttpServer(this)
            server?.start()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
        server = null
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TV Remote Server")
            .setContentText("Server berjalan di port 8080")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TV Remote Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menjaga server remote tetap berjalan"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "tv_remote_channel"
        const val NOTIFICATION_ID = 1
    }
}
