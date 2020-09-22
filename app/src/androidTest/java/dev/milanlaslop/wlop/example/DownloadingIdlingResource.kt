import android.app.Activity
import android.support.test.espresso.IdlingResource
import android.support.test.espresso.IdlingResource.ResourceCallback
import android.widget.TextView
import dev.milanlaslop.wlop.example.R

class DownloadingIdlingResource(private var activity: Activity) : IdlingResource {
    private var resourceCallback: ResourceCallback? = null

    override fun getName(): String {
        return DownloadingIdlingResource::javaClass.name
    }

    override fun isIdleNow(): Boolean {
        var view = activity.findViewById<TextView>(R.id.state);

        var isIdle = (view.text != "Downloading...")

        if (isIdle) {
            resourceCallback!!.onTransitionToIdle()
        }

        return isIdle
    }

    override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback) {
        this.resourceCallback = resourceCallback
    }
}