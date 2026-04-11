package com.github.eylulnc.aura.di

import androidx.room.Room
import com.github.eylulnc.aura.repository.AuraDatabase
import com.github.eylulnc.aura.repository.MoodRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AuraDatabase::class.java,
            "aura.db"
        ).build()
    }

    single { get<AuraDatabase>().moodDao() }

    single { MoodRepository(get()) }
}
