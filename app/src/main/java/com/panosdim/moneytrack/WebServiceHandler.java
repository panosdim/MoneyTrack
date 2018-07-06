package com.panosdim.moneytrack;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WebServiceHandler {
    private static final String TAG = "MT_WEB_SERVICE";

    public String performPostCall(String requestURL,
                                  JSONObject jsonParam) {
        URL url;
        StringBuilder response = new StringBuilder();
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            DataOutputStream printout = new DataOutputStream(conn.getOutputStream());
            printout.writeBytes(jsonParam.toString());
            printout.flush();
            printout.close();


            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
