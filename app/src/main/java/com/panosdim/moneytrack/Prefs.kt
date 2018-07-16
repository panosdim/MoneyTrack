package com.panosdim.moneytrack

import android.content.Context
import android.content.SharedPreferences

class Prefs (context: Context) {
    val PREFS_FILENAME = "com.panosdim.moneytrack.prefs"
    val PHPSESSID = "php_session_id"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0);

    var phpSession: String
        get() = prefs.getString(PHPSESSID, "")
        set(value) = prefs.edit().putString(PHPSESSID, value).apply()
}