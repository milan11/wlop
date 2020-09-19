package dev.milanlaslop.wlop

import android.content.Context
import android.graphics.PixelFormat
import android.view.WindowManager

class WindowManagerWebViewHost(context: Context) : IWebViewHost {
    private val context: Context = context.applicationContext

    override fun createWebView(): CustomWebView {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            0,
            PixelFormat.TRANSLUCENT
        )

        val webView = CustomWebView(context)

        windowManager.addView(webView, params)

        return webView
    }

    override fun removeWebView(webView: CustomWebView) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.removeView(webView)
    }

    override fun refreshParentVisibility() {
    }
}