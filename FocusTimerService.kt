package com.example.focusguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class FocusTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val channelId = "focusguard_timer"

    private val tick = object : Runnable {
        override fun run() {
            if (!FocusStore.isActive(this@FocusTimerService)) {
                FocusStore.clear(this@FocusTimerService)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }
            val seconds = ((FocusStore.endAt(this@FocusTimerService) - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            val notification = buildNotification("专注进行中", "剩余 ${seconds / 60} 分 ${seconds % 60} 秒")
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(1, notification)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification("FocusGuard", "正在准备专注计时"))
        handler.post(tick)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "专注计时", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, channelId) else Notification.Builder(this)
        return builder.setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(FocusStore.isActive(this))
            .build()
    }
}
