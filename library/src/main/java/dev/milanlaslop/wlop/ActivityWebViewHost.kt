package dev.milanlaslop.wlop

import android.view.View
import android.view.ViewGroup

class ActivityWebViewHost(private val parent: ViewGroup) : IWebViewHost {
    override fun createWebView(): CustomWebView {
        val webView = CustomWebView(parent.context)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        parent.addView(webView)
        return webView
    }

    override fun removeWebView(webView: CustomWebView) {
        parent.removeView(webView)
    }

    override fun refreshParentVisibility() {
        var somethingVisible = false
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.visibility == View.VISIBLE) {
                somethingVisible = true
                break
            }
        }

        parent.visibility = if (somethingVisible) View.VISIBLE else View.GONE;
    }
}