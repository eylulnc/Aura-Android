package com.github.eylulnc.aura

import android.app.Application
import com.github.eylulnc.aura.di.appModule
import com.github.eylulnc.aura.preferences.AppPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

class AuraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AuraApplication)
            modules(appModule)
        }
        getKoin().get<AppPreferences>().recordFirstLaunchIfNeeded()
    }
}
