package com.panosdim.moneytrack

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.panosdim.moneytrack.api.Webservice
import com.panosdim.moneytrack.api.data.LoginRequest
import com.panosdim.moneytrack.api.webservice
import com.panosdim.moneytrack.utils.checkForNewVersion
import com.panosdim.moneytrack.utils.refId
import com.panosdim.moneytrack.utils.startIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {
    private lateinit var manager: DownloadManager
    private lateinit var onComplete: BroadcastReceiver
    private var client: Webservice = webservice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        // Check for new version of the app
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RC.PERMISSION_REQUEST.code
            )
        }

        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            checkForNewVersion(this@MainActivity)
        }

        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val referenceId = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (referenceId != -1L && referenceId == refId) {
                    val apkUri = manager.getUriForDownloadedFile(refId)
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    installIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(installIntent)
                }

            }
        }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onResume() {
        super.onResume()

        refreshToken()
    }

    private fun refreshToken() {
        if (prefs.email.isNotEmpty() and prefs.password.isNotEmpty()) {
            val scope = CoroutineScope(Dispatchers.Main)

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val response =
                            client.login(
                                LoginRequest(
                                    prefs.email, prefs.password
                                )
                            )
                        prefs.token = response.token
                    }
                } catch (e: HttpException) {
                    startIntent(this@MainActivity, LoginActivity::class.java)
                }
            }
        } else {
            startIntent(this, LoginActivity::class.java)
        }
    }
}