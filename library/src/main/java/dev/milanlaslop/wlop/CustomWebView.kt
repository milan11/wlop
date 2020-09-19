package dev.milanlaslop.wlop

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebView : WebView {

    private var isTextEditor : Boolean? = null

    internal var javaScriptInterface : JavaScriptInterface
        private set

    init {
        javaScriptInterface = JavaScriptInterface()
        addJavascriptInterface(javaScriptInterface, "HTMLOUT")

        webViewClient = object : WebViewClient() {
        }

        isTextEditor = true
        isFocusableInTouchMode = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        };
        CookieManager.getInstance().setAcceptCookie(true)

        clearCache(true)
        clearHistory()
        settings.javaScriptEnabled = true

        visibility = WebView.GONE
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onCheckIsTextEditor(): Boolean {
        return if (isTextEditor == null) {
            super.onCheckIsTextEditor()
        } else isTextEditor!!
    }
}