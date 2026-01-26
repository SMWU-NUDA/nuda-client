package com.nuda.nudaclient

import android.app.Application
import com.nuda.nudaclient.data.remote.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.init(this)
    }
}