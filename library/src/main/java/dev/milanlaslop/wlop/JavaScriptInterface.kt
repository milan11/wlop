package dev.milanlaslop.wlop

import android.webkit.JavascriptInterface

internal class JavaScriptInterface {

    private val lock = Object()

    private var stringOutput : String? = null

    @JavascriptInterface
    fun provideStringOutput(str: String) {
        synchronized(lock) {
            this.stringOutput = str
            lock.notify()
        }
    }

    fun resetStringOutput() {
        stringOutput = null
    }

    fun getStringOutput() : String {
        synchronized(lock) {
            lock.wait(500)
        }
        return stringOutput.orEmpty()
    }

}