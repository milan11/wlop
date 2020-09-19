package dev.milanlaslop.wlop

interface IWebViewHost {
    fun createWebView() : CustomWebView
    fun removeWebView(webView: CustomWebView)
    fun refreshParentVisibility()
}