package com.personal.thesystem.data

import android.content.Context
import com.personal.thesystem.BuildConfig
import com.yandex.mapkit.MapKitFactory

object MapKitRuntime {
    private var initialized = false
    private var started = false

    @Synchronized
    fun start(context: Context): Boolean {
        if (BuildConfig.MAPKIT_API_KEY.isBlank()) return false
        if (!initialized) {
            MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
            MapKitFactory.initialize(context.applicationContext)
            initialized = true
        }
        if (!started) {
            MapKitFactory.getInstance().onStart()
            started = true
        }
        return true
    }

    @Synchronized
    fun stop() {
        if (started) {
            MapKitFactory.getInstance().onStop()
            started = false
        }
    }
}
