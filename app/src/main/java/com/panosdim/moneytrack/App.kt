package com.panosdim.moneytrack

import android.app.Application
import androidx.room.Room
import com.panosdim.moneytrack.db.AppDatabase

val prefs: Prefs by lazy {
    App.prefs!!
}

val db by lazy {
    App.db
}

enum class RC(val code: Int) {
    PERMISSION_REQUEST(0)
}

const val BACKEND_URL = "https://api.moneytrack.cc.nf/v2/"

class App : Application() {
    companion object {
        var prefs: Prefs? = null
        lateinit var db: AppDatabase
        lateinit var instance: App private set
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "moneytrack"
        )
                .fallbackToDestructiveMigration()
                .build()
        super.onCreate()
        instance = this
    }
}