package com.example.zhuzhiliao.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.example.zhuzhiliao.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
