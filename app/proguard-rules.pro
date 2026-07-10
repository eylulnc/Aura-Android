# Keep line numbers so crash reports are readable
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *

-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keep class kotlin.Metadata { *; }

-keepnames class org.koin.** { *; }
-keep class org.koin.core.annotation.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

-keep class com.caverock.androidsvg.** { *; }

-keep class coil.** { *; }

-keep class androidx.glance.** { *; }

-keep class com.github.eylulnc.aura.model.** { *; }
-keep class com.github.eylulnc.aura.constants.** { *; }
