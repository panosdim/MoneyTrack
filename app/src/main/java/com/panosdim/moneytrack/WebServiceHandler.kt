package com.panosdim.moneytrack

import android.text.TextUtils
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import javax.net.ssl.HttpsURLConnection
import java.net.*


class WebServiceHandler {
    private val baseURL = "http://moneytrack.cc.nf/"
    private val sessionCookie: HttpCookie? = null
    val COOKIES_HEADER = "Set-Cookie"

    fun performPostCall(requestURL: String,
                        jsonParam: JSONObject): String {
        val url: URL
        var response = StringBuilder()
        try {
            url = URL(baseURL + requestURL)

            val conn = url.openConnection() as HttpURLConnection

            CookieHandler.setDefault(msCookieManager)

            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val printout = DataOutputStream(conn.outputStream)
            printout.writeBytes(jsonParam.toString())
            printout.flush()
            printout.close()


            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val headerFields = conn.getHeaderFields()
                val cookiesHeader = headerFields.get(COOKIES_HEADER)

                for (cookie in cookiesHeader!!) {
                    msCookieManager.cookieStore.add(null, HttpCookie.parse(cookie)[0])
                }

                val br = BufferedReader(InputStreamReader(conn.inputStream))
                for (line in br.lines()) {
                    response.append(line)
                }
            } else {
                response = StringBuilder()

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response.toString()
    }

    fun performGetCall(requestURL: String): String {
        val url: URL
        val response = StringBuilder()
        Log.d(TAG, "Inside Function")
        try {
            url = URL(baseURL + requestURL)

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"

            if (msCookieManager.cookieStore.cookies.size > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.cookieStore.cookies));
            }

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                for (line in br.lines()) {
                    response.append(line)
                }
            } else {
                response.append("ERROR ${conn.responseCode}")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(TAG, response.toString())
        return response.toString()
    }

    companion object {
        private val TAG = "MT_WEB_SERVICE"
        val msCookieManager = CookieManager()
    }
}
