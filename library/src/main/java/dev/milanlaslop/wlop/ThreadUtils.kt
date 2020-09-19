package dev.milanlaslop.wlop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T> onUiThread(work: () -> T) : T {
    var result : T? = null
    withContext(Dispatchers.Main.immediate) {
        result = work()
    }
    return result!!
}