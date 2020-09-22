package dev.milanlaslop.wlop.example

import DownloadingIdlingResource
import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.IdlingPolicies
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.espresso.web.sugar.Web.onWebView
import android.support.test.espresso.web.webdriver.DriverAtoms.*
import android.support.test.espresso.web.webdriver.Locator
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import android.support.test.runner.lifecycle.Stage
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.TimeUnit

@RunWith(Parameterized::class)
class DownloadTest(val id_host : Int, val id_dataEntry : Int, runName: String) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name="{2}")
        fun data() = listOf(
            arrayOf(R.id.config_host_activity, R.id.config_dataEntry_automatic, "activity, automatic"),
            arrayOf(R.id.config_host_activity, R.id.config_dataEntry_manual, "activity, manual"),
            arrayOf(R.id.config_host_windowManager, R.id.config_dataEntry_automatic, "windowManager, automatic")
            //arrayOf(R.id.config_host_windowManager, R.id.config_dataEntry_manual, "windowManager, manual")
        )
    }

    @Test
    fun test() {
        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.startActivity(appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)!!.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

        onView(withId(id_host)).perform(click())
        onView(withId(id_dataEntry)).perform(click())
        onView(withText("Start Download")).perform(click())

        var currentActivity: Activity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            currentActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).elementAtOrNull(0)
        }

        onView(withId(R.id.state)).check(matches(withText("Downloading...")))

        if (id_dataEntry == R.id.config_dataEntry_manual) {
            for (i in 0 until 100) {
                try {
                    onWebView().withElement(findElement(Locator.ID, "user_name")).perform(webClick())
                    break
                } catch (e: Exception) {
                    Thread.sleep(100)
                }
            }

            onWebView().withElement(findElement(Locator.ID, "user_name")).perform(webKeys("a"))
            onWebView().withElement(findElement(Locator.ID, "password")).perform(webKeys("b"))
            onWebView().withElement(findElement(Locator.TAG_NAME, "button")).perform(webClick())
        }

        IdlingPolicies.setMasterPolicyTimeout(30, TimeUnit.SECONDS)
        IdlingPolicies.setIdlingResourceTimeout(30, TimeUnit.SECONDS)
        var downloadingIdlingResource = DownloadingIdlingResource(currentActivity!!)
        IdlingRegistry.getInstance().register(downloadingIdlingResource)

        onView(withId(R.id.state)).check(matches(withText("Download complete")))
        IdlingRegistry.getInstance().unregister(downloadingIdlingResource)
    }
}
