package dev.milanlaslop.wlop

import java.lang.StringBuilder

internal fun escapeJavaScriptString(str : String) : String {
    val sb = StringBuilder()

    sb.append('\'')

    for (char in str) {
        sb.append("\\u")
        sb.append("%04x".format(char.toInt()))
    }

    sb.append('\'')

    return sb.toString()
}