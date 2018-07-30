package com.panosdim.moneytrack

import android.content.Context
import android.content.SharedPreferences

const val PREFS_FILENAME = "com.panosdim.moneytrack.prefs"
const val PHPSESSID = "php_session_id"

class Prefs (context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var phpSession: String
        get() = prefs.getString(PHPSESSID, "")
        set(value) = prefs.edit().putString(PHPSESSID, value).apply()
}