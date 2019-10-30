package com.loenzo.serialtest2.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.loenzo.serialtest2.R

class AlarmBroadcastReceiver: BroadcastReceiver() {
    companion object {
        const val ID = "ID"
        const val TITLE = "TITLE"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent!!.getLongExtra(ID, 0)
        val title = intent.getStringExtra(TITLE)

        val builder = NotificationCompat.Builder(context!!, title!!)
        builder.setContentTitle(context.resources.getString(R.string.app_name))
            .setContentText(context.resources.getString(R.string.check_time, title))
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.small_icon)
            .setLargeIcon((context.resources.getDrawable(R.drawable.icon, null) as BitmapDrawable).bitmap)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(id.toInt(), builder.build())
    }

}