package dev.milanlaslop.wlop

class WebViewSource {
    private var host : IWebViewHost? = null

    private val activeWebViews = mutableMapOf<WebViewAccess, CustomWebView>()

    suspend fun registerHost(host: IWebViewHost) {
        onUiThread {
            if (this.host != null) {
                throw InvalidSourceStateException("Host already registered")
            }
            host.refreshParentVisibility()
            this.host = host
        }
    }

    suspend fun unregisterHost() {
        onUiThread {
            val host = this.host ?: throw InvalidSourceStateException("Host not registered")
            for (webView in activeWebViews.values) {
                host.removeWebView(webView)
            }
            host.refreshParentVisibility()
            this.host = null

            activeWebViews.clear()
        }
    }

    suspend fun hostUsed() : Boolean {
        return onUiThread {
            return@onUiThread activeWebViews.isNotEmpty()
        }
    }

    suspend fun <T> useOneWebView(work: suspend (WebViewAccess) -> T) : T {
        val access = onUiThread {
            val host = this.host ?: throw InvalidSourceStateException("Host not registered")
            val webView = host.createWebView()
            host.refreshParentVisibility()
            val access = WebViewAccess(this, webView.javaScriptInterface)
            activeWebViews[access] = webView

            return@onUiThread access
        }

        return access.use {
            return work(it)
        }
    }

    internal suspend fun <T> withWebView(access: WebViewAccess, work: (CustomWebView) -> T) : T {
        return onUiThread {
            val webView = activeWebViews.get(access) ?: throw WebViewDoesNotExistException()

            val result = work(webView)
            host!!.refreshParentVisibility()
            return@onUiThread result
        }
    }

    internal suspend fun finishWebView(access: WebViewAccess) {
        onUiThread {
            val webView = activeWebViews.remove(access)
            if (webView == null) {
                throw WebViewDoesNotExistException()
            } else {
                val host = this.host ?: throw InvalidSourceStateException("Host not registered")
                host.removeWebView(webView)
            }
        }
    }
}