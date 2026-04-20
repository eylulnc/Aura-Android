package com.github.eylulnc.aura.di

import androidx.room.Room
import com.github.eylulnc.aura.auth.AuthRepository
import com.github.eylulnc.aura.notification.NotificationScheduler
import com.github.eylulnc.aura.preferences.AppPreferences
import com.github.eylulnc.aura.repository.AuraDatabase
import com.github.eylulnc.aura.repository.MoodRepository
import com.github.eylulnc.aura.ui.history.HistoryViewModel
import com.github.eylulnc.aura.ui.settings.SettingsViewModel
import com.github.eylulnc.aura.viewmodel.TodayViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AuraDatabase::class.java,
            "aura.db"
        ).addMigrations(AuraDatabase.MIGRATION_1_2).build()
    }

    single { get<AuraDatabase>().moodDao() }

    single { AuthRepository() }

    single { MoodRepository(get(), get()) }

    single { AppPreferences(androidContext()) }

    single { NotificationScheduler(androidContext()) }

    viewModel { TodayViewModel(androidApplication(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
}
