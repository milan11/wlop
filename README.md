# Wlop

Android library for automated web browsing and content downloading in a hidden `WebView`. The `WebView` automatically handles JavaScript execution, redirects and other browsing features. This library encapsulates a lot of details to make the usage more convenient.

## Features

- WebView hosting in an `Activity` or using `WindowManager` (outside of any activities)
- WebView lifetime management not causing `Activity` or `Context` memory leaks (when the hosting activity is closed, the WebView is removed causing exception for all its users)
- WebView accessing from any thread (usable for e.g. `IntentService` or `AsyncTask`) - transition to UI thread and back using Kotlin Coroutines
- executing of JavaScript snippets, getting output from JavaScript
- simple automation utility methods - clicking on element, filling a field...
- content downloading automatically following redirects and sharing cookies with the `WebView`
- utility methods for cookies management (globally for all `WebView`s in the application)

## Building and Installation

./buildLibrary.sh or:

```
./gradlew build publishToMavenLocal
```

The library will be published to your local Maven repository.

To use the library in you application, add to `build.gradle`:

```
repositories {
    ...
    mavenLocal()
}

...

dependencies {
    ...
    implementation 'dev.milanlaslop.wlop:wlop:0.13.0'
}
```

## Example

Open `wlop.iml` in Android Studio. The example application `app` allows testing of some scenarios. The code working with the `WebView` is in the `DownloadService` class.

## Usage

### WebViewSource

Define static `WebViewSource` somewhere, e.g. in a service which will access the `WebView`:

```
class DownloadService ... {

    companion object {
        var webViewSource = WebViewSource()
    }
    ...

}
```

### IWebViewHost

Choose where the `WebView` will reside. There are currently 2 possible hosts (each with its own disadvantages).

#### ActivityWebViewHost

Prepare some `View` (must be `ViewGroup`, e.g. `LinearLayout`) where the `WebView` will be created.
The visibility of this `ViewGroup` will be managed automatically (it will be hidden unless a visible `WebView` is required e.g. for user to provide some input).

```
<LinearLayout
    android:id="@+id/webViewWrapper"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    />
```

Register this host in `onCreate` of the `Activity`:

```
runBlocking {
    DownloadService.webViewSource.registerHost(ActivityWebViewHost(findViewById(R.id.webViewWrapper)))
}
```

Unregister this host in `onDestroy` of the `Activity`:

```
runBlocking {
    DownloadService.webViewSource.unregisterHost()
}
```

Optionally, handle `onBackPressed` to warn the user when leaving the `Activity` if the `WebView` residing in that `Activity` is being used:

```
private var backTried = false

override fun onBackPressed() {
    runBlocking {
        if (DownloadService.webViewSource.hostUsed()) {
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
```

This host has a disadvantage that it does not work without an existing `Activity`.

#### WindowManagerWebViewHost

Register this host in `onCreate` of the `Application`:

```
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        runBlocking {
            DownloadService.webViewSource.registerHost(WindowManagerWebViewHost(applicationContext))
        }
    }
}
```

Of course, this `Application` class must be registered in `AndroidManifest.xml`

```
<application
    android:name=".Application"
    ...
```

This host has a disadvantage that it needs a permission:

```
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

The user can grant the permission by going to application settings - Display over other apps.

### WebViewAccess

To work with the `WebView`:

```
try {
    runBlocking {
        webViewSource.useOneWebView {
            it.withWebView(...)
            it.executeJavaScript(...)
            it.getStringFromJavaScript(...)
            it.startGoingTo(...)
            it.getHtml(...)
            it.waitForHtml(...)
            it.click(...)
            it.fillField(...)
            it.waitForFieldFilled(...)
            it.scrollDown(...)
            it.userInputRequired(...)
            it.continueHidden(...)
            it.download(...)
            it.sleep(...)
        }
    }

} catch (t : WebViewDoesNotExistException) {
    // WebView was abandoned (e.g. the Activity was closed)
} catch (t : TimeoutException) {
    // waiting for too long (e.g. for user to provide input, or for server to respond)
} catch (t : Throwable) {
    // other error
}
```

### GlobalWebViewUtils

Some actions influence all `WebView`s in the application (most notably cookies management using `CookieManager`).
The library provides some methods encapsulating these actions:

```
setWebViewDebuggingEnabled
getCookies
setCookies
clearAllCookies
```

## Dependencies

- Kotlin libraries - Standard Library and Coroutines
- [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) - for creating a test server in the example application
