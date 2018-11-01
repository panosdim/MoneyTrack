package com.panosdim.moneytrack.network

import android.text.TextUtils
import com.panosdim.moneytrack.prefs
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.*
import javax.net.ssl.HttpsURLConnection


class WebServiceHandler {
    private val baseURL = "https://moneytrack.cc.nf/api/v2/"
    private val cookiesHeader = "Set-Cookie"

    fun performPostCall(requestURL: String,
                        jsonParam: JSONObject): String {
        val url: URL
        var response = ""
        try {
            url = URL(baseURL + requestURL)

            val conn = url.openConnection() as HttpURLConnection

            CookieHandler.setDefault(msCookieManager)

            conn.readTimeout = 5000
            conn.connectTimeout = 5000
            conn.requestMethod = "POST"
            conn.doOutput = true

            if (msCookieManager.cookieStore.cookies.size == 0) {
                conn.setRequestProperty("Cookie", prefs.phpSession)
            } else {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.cookieStore.cookies))
            }
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

            val printout = DataOutputStream(conn.outputStream)
            printout.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
            printout.flush()
            printout.close()

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val headerFields = conn.headerFields
                val cookiesHeader = headerFields[cookiesHeader]
                if (cookiesHeader != null) {
                    for (cookie in cookiesHeader) {
                        if (cookie.contains("PHPSESSID")) {
                            prefs.phpSession = cookie
                            msCookieManager.cookieStore.add(null, HttpCookie.parse(cookie)[0])
                        }
                    }
                }

                response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val json = JSONObject()
            json.put("success", false)
            json.put("message", "An Network error occurred please check you are connected to internet.")
            response = json.toString()
        }

        return response
    }

    fun performGetCall(requestURL: String): String {
        val url: URL
        var response = ""
        try {
            url = URL(baseURL + requestURL)

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 5000
            conn.connectTimeout = 5000
            conn.requestMethod = "GET"

            if (msCookieManager.cookieStore.cookies.size == 0) {
                conn.setRequestProperty("Cookie", prefs.phpSession)
            } else {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.cookieStore.cookies))
            }

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val json = JSONObject()
            json.put("success", false)
            json.put("message", "An Network error occurred please check you are connected to internet.")
            response = json.toString()
        }

        return response
    }

    companion object {
        val msCookieManager = CookieManager()
    }
}
