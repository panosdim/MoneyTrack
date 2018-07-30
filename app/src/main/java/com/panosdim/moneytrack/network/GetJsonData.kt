package com.panosdim.moneytrack.network

import android.os.AsyncTask

class GetJsonData(private val mCallback: (result: String) -> Unit) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String {
        val wsh = WebServiceHandler()
        return wsh.performGetCall(params[0])
    }

    override fun onPostExecute(result: String) {
        mCallback(result)
    }
}