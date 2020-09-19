package dev.milanlaslop.wlop

import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebView

suspend fun setWebViewDebuggingEnabled(enabled : Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        onUiThread {
            WebView.setWebContentsDebuggingEnabled(enabled)
        }
    }
}

fun getCookies(url: String) : String {
    val cookieManager = android.webkit.CookieManager.getInstance()

    return cookieManager.getCookie(url).orEmpty()
}

fun setCookies(url: String, cookie: String) {
    val cookieManager = android.webkit.CookieManager.getInstance()

    for (cookieItem in cookie.split(";")) {
        cookieManager.setCookie(url, cookieItem)
    }
}

fun clearAllCookies() {
    CookieManager.getInstance().removeAllCookie()
    CookieManager.getInstance().removeSessionCookie()
}