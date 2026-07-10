package com.github.eylulnc.aura

import android.app.Application
import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.di.appModule
import com.github.eylulnc.aura.notification.NotificationHelper
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.widget.AuraMoodWidgetReceiver
import com.github.eylulnc.aura.widget.AuraStreakWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

class AuraApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AuraApplication)
            modules(appModule)
        }
        getKoin().get<AppPreferences>().recordFirstLaunchIfNeeded()
        NotificationHelper.createChannel(this)
        refreshWidgetsOnAuthChange()
    }

    // When the signed-in user changes, widgets need to re-query Room for the new
    // user's data; otherwise they keep showing the previous user's state.
    private fun refreshWidgetsOnAuthChange() {
        val auth = getKoin().get<AuthRepository>()
        appScope.launch {
            auth.authStateFlow.drop(1).collect {
                AuraMoodWidgetReceiver.requestUpdate(this@AuraApplication)
                AuraStreakWidgetReceiver.requestUpdate(this@AuraApplication)
            }
        }
    }
}
