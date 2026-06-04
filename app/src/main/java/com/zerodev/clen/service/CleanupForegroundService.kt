package com.zerodev.clen.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class CleanupForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
