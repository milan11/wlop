package dev.milanlaslop.wlop

import java.lang.Exception

class InvalidSourceStateException(message : String) : Exception(message)
class WebViewDoesNotExistException : Exception("WebView does not exist anymore")
class TimeoutException(message: String) : Exception(message)