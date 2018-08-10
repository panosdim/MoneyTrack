package com.panosdim.moneytrack

import android.app.Application

val prefs: Prefs by lazy {
    App.prefs!!
}

val categories: MutableList<Category> by lazy {
    App.categories
}

class App : Application() {
    companion object {
        var prefs: Prefs? = null
        var categories: MutableList<Category> = mutableListOf()
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        super.onCreate()
    }
}