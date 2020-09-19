package dev.milanlaslop.wlop.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import dev.milanlaslop.wlop.ActivityWebViewHost
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private var backTried = false

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            findViewById<Button>(R.id.start).isEnabled = true
            findViewById<TextView>(R.id.state).text = intent!!.getStringExtra("result")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        runBlocking {
            DownloadService.webViewSource_activity.registerHost(ActivityWebViewHost(findViewById(R.id.webViewWrapper)))
        }

        val filter = IntentFilter()
        filter.addAction(Companion.BROADCAST_ACTION_RESULT)
        registerReceiver(receiver, filter);
    }

    override fun onDestroy() {
        super.onDestroy()

        runBlocking {
            DownloadService.webViewSource_activity.unregisterHost()
        }

        unregisterReceiver(receiver);
    }

    fun startDownload(view: View) {
        val host = when(findViewById<RadioGroup>(R.id.config_host).checkedRadioButtonId) {
            R.id.config_host_activity -> "activity"
            R.id.config_host_windowManager -> "window_manager"
            else -> throw Exception("Invalid host selected")
        }

        val dataEntry = when(findViewById<RadioGroup>(R.id.config_dataEntry).checkedRadioButtonId) {
            R.id.config_dataEntry_automatic -> "automatic"
            R.id.config_dataEntry_manual -> "manual"
            else -> throw Exception("Invalid data entry mode selected")
        }

        startService(Intent(this, DownloadService::class.java)
            .putExtra("host", host)
            .putExtra("data_entry", dataEntry)
        )

        findViewById<Button>(R.id.start).isEnabled = false
        findViewById<TextView>(R.id.state).text = "Downloading..."
    }

    override fun onBackPressed() {
        runBlocking {
            if (DownloadService.webViewSource_activity.hostUsed()) {
                if (!backTried) {
                    Toast.makeText(this@MainActivity, "Going back can interrupt current download. Press Back again to really go back.", Toast.LENGTH_SHORT).show()
                    backTried = true
                } else {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    companion object {
        const val BROADCAST_ACTION_RESULT = "dev.milanlaslop.wlop.broadcast.result"
    }
}
