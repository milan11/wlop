package dev.milanlaslop.wlop.example

import android.app.Application
import dev.milanlaslop.wlop.WindowManagerWebViewHost
import kotlinx.coroutines.runBlocking

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        runBlocking {
            DownloadService.webViewSource_windowManager.registerHost(WindowManagerWebViewHost(applicationContext))
        }
    }
}