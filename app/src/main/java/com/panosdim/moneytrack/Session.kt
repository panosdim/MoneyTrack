package com.panosdim.moneytrack

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.json.JSONException
import org.json.JSONObject

class Session : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mJsonTask = GetJsonDataTask("php/session.php")
        mJsonTask.execute(null as Void?)
    }

    inner class GetJsonDataTask internal constructor(private val url: String) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void): String? {
            val wsh = WebServiceHandler()
            return wsh.performGetCall(url)
        }

        override fun onPostExecute(result: String?) {
            Log.d("MT_APP", result)
            try {
                val resp = JSONObject(result)
                if (resp.getBoolean("loggedIn")) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(applicationContext, LoginActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}