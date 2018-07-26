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
    private val baseURL = "http://moneytrack.cc.nf/"
    private val cookiesHeader = "Set-Cookie"

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
                val headerFields = conn.headerFields
                val cookiesHeader = headerFields[cookiesHeader]

                for (cookie in cookiesHeader!!) {
                    if (cookie.contains("PHPSESSID")) {
                        prefs.phpSession = cookie
                        msCookieManager.cookieStore.add(null, HttpCookie.parse(cookie)[0])
                    }
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
        try {
            url = URL(baseURL + requestURL)

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
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
        return response.toString()
    }

    companion object {
        val msCookieManager = CookieManager()
    }
}
