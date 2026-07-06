package com.yiqiu.readingquiz

import android.app.Application
import com.yiqiu.readingquiz.data.ReadingRepository

class ReadingQuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReadingRepository.init(this)
    }
}