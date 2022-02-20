package dev.milanlaslop.wlop

import android.view.View
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.BufferedWriter
import java.io.Closeable
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class WebViewAccess internal constructor(private val source : WebViewSource, private val javaScriptInterface : JavaScriptInterface) : Closeable {
    override fun close() {
        runBlocking {
            source.finishWebView(this@WebViewAccess)
        }
    }

    suspend fun <T> withWebView(work: (CustomWebView) -> T) : T {
        return source.withWebView(this, work)
    }

    suspend fun executeJavaScript(code: String) {
        withWebView {
            it.loadUrl("javascript:(function(){$code})();")
        }
    }

    suspend fun getStringFromJavaScript(code: String) : String {
        withWebView {
            javaScriptInterface.resetStringOutput()
        }

        executeJavaScript("javascript:window.HTMLOUT.provideStringOutput($code);")
        val result : String = javaScriptInterface.getStringOutput()

        withWebView {
            javaScriptInterface.resetStringOutput()
        }

        return result
    }

    suspend fun startGoingTo(url : String) {
        withWebView {
            it.loadUrl(url)
        }
    }

    suspend fun getHtml() : String {
        return getStringFromJavaScript("'<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'")
    }

    suspend fun waitForHtml(timeoutMs : Long, check : (String) -> Boolean) : String {
        val timeBegin = System.nanoTime()

        while (System.nanoTime() < timeBegin + timeoutMs * 1000 * 1000) {
            val html : String = getHtml()
            if (check(html)) {
                return html
            }
            delay(50)
        }

        throw TimeoutException("Waiting for HTML timed out")
    }

    suspend fun click(element : ElementSpec) {
        executeJavaScript("${element.getJs()}.click();")
    }

    suspend fun fillField(element : ElementSpec, value : String) {
        val escaped_value = escapeJavaScriptString(value)
        executeJavaScript("${element.getJs()}.value = $escaped_value;")
    }

    suspend fun waitForFieldFilled(element : ElementSpec, value : String, timeoutMs: Long) {
        val timeBegin = System.nanoTime()

        while (System.nanoTime() < timeBegin + timeoutMs * 1000 * 1000) {
            val result = getStringFromJavaScript("${element.getJs()}.value")
            if (result == value) {
                return
            }
        }

        throw TimeoutException("Waiting for field filled timed out")
    }

    suspend fun scrollDown() {
        withWebView {
            it.scrollBy(0, 500)
        }
    }

    suspend fun userInputRequired() {
        withWebView {
            it.visibility = View.VISIBLE
        }
    }

    suspend fun continueHidden() {
        withWebView {
            it.visibility = View.GONE
        }
    }

    suspend fun download(url: String, postDataOrNull: String?) : HttpURLConnection {
        var conn = URL(url).openConnection() as HttpURLConnection

        val userAgent = withWebView {
            it.settings.userAgentString
        }

        var count = 0
        while (true) {
            conn.instanceFollowRedirects = false
            conn.setRequestProperty("User-Agent", userAgent)
            currentCookiesToRequest(conn)

            if (count == 0 && postDataOrNull != null) {
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.use { os ->
                    val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                    writer.write(postDataOrNull)

                    writer.flush()
                    writer.close()
                }
            } else {
                conn.requestMethod = "GET"
                conn.doOutput = false
            }

            if (conn.responseCode in 200..299) {
                responseToCurrentCookies(conn)
                return conn
            }
            else if (conn.responseCode in 300..399) {
                val location = conn.getHeaderField("Location")

                var absoluteLocation = when {
                    location.startsWith("//") -> {
                        conn.url.protocol + "://" + location.substring(2)
                    }
                    location.startsWith('/') -> {
                        val portStr = if (conn.url.port != -1) ":" + conn.url.port; else ""
                        conn.url.protocol + "://" + conn.url.host + portStr + location
                    }
                    else -> {
                        location
                    }
                }

                ++count
                if (count >= 10) {
                    throw IOException("Too many redirects")
                }

                responseToCurrentCookies(conn)

                conn = URL(absoluteLocation).openConnection() as HttpURLConnection
            }
            else {
                throw RuntimeException("Invalid status code (4): " + conn.responseCode)
            }
        }
    }

    suspend fun sleep(ms : Long) {
        delay(ms)
    }

    private fun currentCookiesToRequest(conn: HttpURLConnection) {
        val cookie = getCookies(conn.url.toString())

        if (!cookie.isNullOrBlank()) {
            conn.setRequestProperty("Cookie", cookie)
        }
    }

    private fun responseToCurrentCookies(conn: HttpURLConnection) {
        val cookieHeaderField = conn.getHeaderField("Set-Cookie")
        if (!cookieHeaderField.isNullOrBlank()) {
            setCookies(conn.url.toString(), cookieHeaderField)
        }
    }
}