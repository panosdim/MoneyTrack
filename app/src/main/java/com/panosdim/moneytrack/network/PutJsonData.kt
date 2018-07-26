package com.panosdim.moneytrack.network

import android.os.AsyncTask
import org.json.JSONObject

class PutJsonData(private val mCallback: (result: String) -> Unit, private val mPHPFile: String) : AsyncTask<JSONObject, Void, String>() {

    override fun doInBackground(vararg params: JSONObject): String {
        val wsh = WebServiceHandler()
        return wsh.performPostCall(mPHPFile, params[0])
    }

    override fun onPostExecute(result: String) {
        mCallback(result);
    }
}