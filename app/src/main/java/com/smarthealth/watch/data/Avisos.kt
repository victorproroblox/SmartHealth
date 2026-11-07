package com.smarthealth.watch.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.Notification
import android.os.Build
import com.smarthealth.watch.R
import com.smarthealth.watch.ui.MainActivity

/** Notificación local que se muestra al registrar una actividad. */
object Avisos {
    private const val CANAL = "smarthealth_actividad"

    /** Devuelve true si la notificación se mostró; false si el permiso está denegado. */
    fun actividadRegistrada(context: Context, texto: String, id: Int): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CANAL, "Actividad registrada", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val abrir = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notificacion = Notification.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("SmartHealth")
            .setContentText(texto)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notificacion)
        return true
    }
}
