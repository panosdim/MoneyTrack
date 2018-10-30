package com.panosdim.moneytrack

import android.content.Context
import android.content.SharedPreferences

const val PREFS_FILENAME = "com.panosdim.moneytrack.prefs"
const val PHPSESSID = "php_session_id"
const val TOKEN = "token"
const val SERIES = "series"
const val SELECTOR = "selector"

class Prefs(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var phpSession: String
        get() = prefs.getString(PHPSESSID, "")
        set(value) = prefs.edit().putString(PHPSESSID, value).apply()

    var token: String
        get() = prefs.getString(TOKEN, "")
        set(value) = prefs.edit().putString(TOKEN, value).apply()

    var selector: String
        get() = prefs.getString(SELECTOR, "")
        set(value) = prefs.edit().putString(SELECTOR, value).apply()

    var series: String
        get() = prefs.getString(SERIES, "")
        set(value) = prefs.edit().putString(SERIES, value).apply()
}