package dev.milanlaslop.wlop.example

import android.content.Context
import fi.iki.elonen.NanoHTTPD

class TestServer(private val context : Context) : NanoHTTPD(8084) {
    override fun serve(session: IHTTPSession?): Response {
        if (session?.uri == "/") {
            return newChunkedResponse(Response.Status.OK, "text/html", context.assets.open("${testServerAssetsDir}/login.html"))
        }
        if (session?.uri == "/action_login") {
            session.parseBody(HashMap())
            if (!(session.parms["user_name"] == "a" && session.parms["password"] == "b")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, null, null)
            }

            val response = newChunkedResponse(Response.Status.OK, "text/html", context.assets.open("${testServerAssetsDir}/login_response.html"))
            response.addHeader("Set-Cookie", "session_id=1")
            return response
        }
        if (session?.uri == "/private") {
            if (!containsCookie(session.headers["cookie"]!!, "session_id=1")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, null, null)
            }

            return newChunkedResponse(Response.Status.OK, "text/html", context.assets.open("${testServerAssetsDir}/private.html"))
        }
        if (session?.uri == "/file") {
            if (!containsCookie(session.headers["cookie"]!!, "session_id=1")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, null, null)
            }

            val response = newChunkedResponse(Response.Status.REDIRECT, "text/plain", context.assets.open("${testServerAssetsDir}/file.txt"))
            response.addHeader("Location", "/file_real")
            response.addHeader("Set-Cookie", "download_stage=1")
            return response
        }
        if (session?.uri == "/file_real") {
            if (!containsCookie(session.headers["cookie"]!!, "session_id=1")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, null, null)
            }

            if (!containsCookie(session.headers["cookie"]!!, "download_stage=1")) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, null, null)
            }

            val response = newChunkedResponse(Response.Status.OK, "text/plain", context.assets.open("${testServerAssetsDir}/file.txt"))
            response.addHeader("Set-Cookie", "download_stage=2")
            return response
        }
        if (session?.uri == "/check_download") {
            if (!containsCookie(session.headers["cookie"]!!, "session_id=1")) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, null, null)
            }

            if (!containsCookie(session.headers["cookie"]!!, "download_stage=2")) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, null, null)
            }

            return newChunkedResponse(Response.Status.OK, "text/html", context.assets.open("${testServerAssetsDir}/check_download.html"))
        }

        return super.serve(session)
    }

    companion object {
        const val testServerAssetsDir = "test_server"

        private fun containsCookie(cookies : String, cookie : String) : Boolean {
            return cookies.split(';').any { it.trim() == cookie}
        }
    }
}