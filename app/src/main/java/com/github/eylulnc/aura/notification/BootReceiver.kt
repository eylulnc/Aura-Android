package com.github.eylulnc.aura.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.eylulnc.aura.preferences.AppPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val prefs: AppPreferences by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!prefs.isNotificationsEnabled()) return
        NotificationScheduler(context).schedule(prefs.getReminderHour(), prefs.getReminderMinute())
    }
}
