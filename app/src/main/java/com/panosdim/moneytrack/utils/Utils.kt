package com.panosdim.moneytrack.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.activities.LoginActivity
import com.panosdim.moneytrack.activities.MainActivity
import org.json.JSONArray
import retrofit2.HttpException
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.Normalizer
import javax.net.ssl.HttpsURLConnection


val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

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
                Version(JSONArray(response).getJSONObject(0).getJSONObject("apkData").getString("versionName"))
            val appVersion =
                Version(context.packageManager.getPackageInfo(context.packageName, 0).versionName)
            if (version.isGreater(appVersion)) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    downloadNewVersion(context)
                } else {
                    ActivityCompat.requestPermissions(
                        context as MainActivity,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        RC.PERMISSION_REQUEST.code
                    )
                }
            }

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun downloadNewVersion(context: Context) {
    val url: URL
    try {

        var destination =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canonicalPath + "/"
        val fileName = "Moneytrack.apk"
        destination += fileName

        //Delete update file if exists
        val file = File(destination)
        if (file.exists())
            file.delete()

        url = URL(BACKEND_URL + "apk/app-release.apk")

        val conn = url.openConnection() as HttpURLConnection

        conn.readTimeout = 15000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"

        val responseCode = conn.responseCode

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            conn.inputStream.use { input ->
                File(destination).outputStream().use { fileOut ->
                    input.copyTo(fileOut)
                }
            }

            val downloads =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val apk = File(downloads, fileName)
            val apkUri =
                FileProvider.getUriForFile(context, context.packageName + ".fileprovider", apk)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}