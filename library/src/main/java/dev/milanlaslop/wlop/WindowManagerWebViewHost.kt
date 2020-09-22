package dev.milanlaslop.wlop

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
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

    companion object {
        fun hasPermission(context: Context) : Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                return false
            }
        }

        fun askForPermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()))
                context.startActivity(intent)
            }
        }
    }
}