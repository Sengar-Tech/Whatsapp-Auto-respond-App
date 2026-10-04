package com.yourfirm.autoreply

import android.app.Application
import com.yourfirm.autoreply.db.AppDatabase
import com.yourfirm.autoreply.util.PrefsManager

/**
 * Application class — created once when the process starts, destroyed when it ends.
 *
 * Both [database] and [prefs] are lazy so they are only created when first accessed,
 * not at process startup. Using the applicationContext prevents memory leaks.
 */
class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val prefs: PrefsManager by lazy { PrefsManager(this) }
}
