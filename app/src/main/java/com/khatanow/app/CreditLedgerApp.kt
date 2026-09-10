package com.khatanow.app

import android.app.Application
import com.khatanow.app.data.local.AppDatabase

class CreditLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Room Database on application startup
        AppDatabase.getDatabase(this)
    }
}
