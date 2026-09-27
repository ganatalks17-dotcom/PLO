package com.ganatalks.personalloanofficer.callback

import android.app.*
import android.content.*
import androidx.core.app.NotificationCompat
import com.ganatalks.personalloanofficer.R

class CallbackAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "callback_reminders"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(channelId,"Callback Reminders",NotificationManager.IMPORTANCE_HIGH))
        }
        val name=intent.getStringExtra("name") ?: "Customer"
        val number=intent.getStringExtra("number") ?: ""
        val notification=NotificationCompat.Builder(context,channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("CUSTOMER CALLBACK REMINDER")
            .setContentText("$name • $number")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify((System.currentTimeMillis()%100000).toInt(),notification)
    }
}
