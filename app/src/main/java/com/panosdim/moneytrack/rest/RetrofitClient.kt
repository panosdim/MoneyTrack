package com.panosdim.moneytrack.rest

import com.google.gson.GsonBuilder
import com.panosdim.moneytrack.BACKEND_URL
import com.panosdim.moneytrack.prefs
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val webservice: Webservice by lazy {
    val client = OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", " Bearer " + prefs.token)
            .build()
        chain.proceed(newRequest)
    }.build()
    Retrofit.Builder()
        .baseUrl(BACKEND_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build().create(Webservice::class.java)
}