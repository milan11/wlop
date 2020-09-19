package dev.milanlaslop.wlop.example

import android.app.IntentService
import android.content.Intent
import dev.milanlaslop.wlop.*
import kotlinx.coroutines.runBlocking

class DownloadService : IntentService("Download") {

    companion object {
        var webViewSource_activity = WebViewSource()
        var webViewSource_windowManager = WebViewSource()
    }

    override fun onHandleIntent(intent: Intent?) {
        var webViewSource = when(intent!!.extras!!["host"]) {
            "activity" -> webViewSource_activity
            "window_manager" -> webViewSource_windowManager
            else -> throw Exception("Invalid host specified")
        }

        var dataEntryAutomatic = when(intent.extras!!["data_entry"]) {
            "automatic" -> true
            "manual" -> false
            else -> throw Exception("Invalid data entry mode specified")
        }

        val server = TestServer(applicationContext)
        server.start()
        try {
            runBlocking {
                clearAllCookies();

                var state = ""
                webViewSource.useOneWebView {
                    it.startGoingTo("http://localhost:8084")
                    it.waitForHtml(2000) {
                        it.contains("Log in")
                    }
                    it.sleep(1000)

                    if (dataEntryAutomatic) {
                        it.fillField(ElementSpec.byId("user_name"), "a")
                        it.fillField(ElementSpec.byId("password"), "b")
                        it.waitForFieldFilled(ElementSpec.byId("user_name"), "a", 2000);
                        it.waitForFieldFilled(ElementSpec.byId("password"), "b", 2000);
                        it.click(ElementSpec.bySelector("[type=\"submit\"]"))
                    }
                    if (!dataEntryAutomatic) {
                        it.userInputRequired()
                    }

                    it.waitForHtml(if (dataEntryAutomatic) 2000 else 30000) {
                        it.contains("Logged in")
                    }

                    if (!dataEntryAutomatic) {
                        it.continueHidden()
                    }

                    state = getCookies("http://localhost:8084")
                }

                clearAllCookies();

                webViewSource.useOneWebView {
                    setCookies("http://localhost:8084", state)
                    it.download("http://localhost:8084/file", null).inputStream.use {
                        if (it.reader().readText() != "test_data") {
                            throw Exception("Invalid data received")
                        }
                    }
                    it.startGoingTo("http://localhost:8084/check_download")
                    it.waitForHtml(2000) {
                        it.contains("Downloaded successfully")
                    }
                }
            }

            showMessage("Download complete")
        } catch (t : WebViewDoesNotExistException) {
            showMessage("Activity was closed")
        } catch (t : TimeoutException) {
            showMessage("Timed out")
        } catch (t : Throwable) {
            showMessage("Download error: " + t.message)
        } finally {
            server.stop()
        }
    }

    private fun showMessage(text : String) {
        val broadcast = Intent()
        broadcast.action = MainActivity.BROADCAST_ACTION_RESULT
        broadcast.putExtra("result", text)
        sendBroadcast(broadcast)
    }
}
