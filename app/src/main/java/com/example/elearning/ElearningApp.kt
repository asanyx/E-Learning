package com.example.elearning

import android.app.Application
import com.example.elearning.data.local.AppDatabase
import com.example.elearning.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElearningApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seedIfEmpty(AppDatabase.getInstance(this@ElearningApp))
        }
    }
}
