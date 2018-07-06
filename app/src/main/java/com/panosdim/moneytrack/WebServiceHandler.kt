package com.panosdim.moneytrack

import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class WebServiceHandler {

    fun performPostCall(requestURL: String,
                        jsonParam: JSONObject): String {
        val url: URL
        var response = StringBuilder()
        try {
            url = URL(requestURL)

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")

            val printout = DataOutputStream(conn.outputStream)
            printout.writeBytes(jsonParam.toString())
            printout.flush()
            printout.close()


            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
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

    companion object {
        private val TAG = "MT_WEB_SERVICE"
    }
}
