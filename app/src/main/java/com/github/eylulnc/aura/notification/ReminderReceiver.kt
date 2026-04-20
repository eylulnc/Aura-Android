package com.github.eylulnc.aura.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.MoodDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver(), KoinComponent {

    private val dao: MoodDao by inject()
    private val prefs: AppPreferences by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().toString()
                val hasEntry = dao.getByDate(today) != null
                if (!hasEntry) {
                    NotificationHelper.post(context)
                }
                // Reschedule for the next day (exact alarms are one-shot)
                if (prefs.isNotificationsEnabled()) {
                    NotificationScheduler(context).schedule(
                        prefs.getReminderHour(),
                        prefs.getReminderMinute()
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
