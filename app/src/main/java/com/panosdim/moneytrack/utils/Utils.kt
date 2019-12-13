package com.panosdim.moneytrack.utils

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.activities.LoginActivity
import com.panosdim.moneytrack.rest.requests.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import retrofit2.HttpException
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import javax.net.ssl.HttpsURLConnection


val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()
var refId: Long = -1

suspend fun downloadData(context: Context) {
    try {
        val responseIncome = App.repository.getAllIncome()
        incomeList.clear()
        incomeList.addAll(responseIncome.data)

        val responseExpenses = App.repository.getAllExpenses()
        expensesList.clear()
        expensesList.addAll(responseExpenses.data)

        val responseCategories = App.repository.getAllCategories()
        categoriesList.clear()
        categoriesList.addAll(responseCategories.data)
        categoriesList.sortByDescending { it.count }
    } catch (e: HttpException) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}

fun checkForNewVersion(context: Context) {
    val url: URL
    val response: String
    try {
        url = URL(BACKEND_URL + "apk/output.json")

        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doOutput = false

        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            val version =
                JSONArray(response).getJSONObject(0).getJSONObject("apkData").getLong("versionCode")
            val appVersion =
                context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
            if (version > appVersion && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                downloadNewVersion(context)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun downloadNewVersion(context: Context) {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request =
        DownloadManager.Request(Uri.parse(BACKEND_URL + "apk/app-release.apk"))
    request.setDescription("Downloading new version of MoneyTrack.")
    request.setTitle("New MoneyTrack Version")
    request.allowScanningByMediaScanner()
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Warehouse.apk")
    refId = manager.enqueue(request)
}

suspend fun loginWithStoredCredentials(context: Context, task: suspend () -> Unit) {
    if (prefs.email.isNotEmpty() and prefs.password.isNotEmpty()) {
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response =
                        repository.login(
                            LoginRequest(
                                prefs.email, prefs.password
                            )
                        )
                    prefs.token = response.token
                }

                task()
            } catch (e: HttpException) {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    } else {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}