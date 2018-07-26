package com.panosdim.moneytrack

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_income_details.*

class IncomeDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_details)
        setSupportActionBar(toolbar)

        val bundle = intent.extras
        val income = bundle!!.getParcelable<Parcelable>(INCOME_MESSAGE) as Income
        Log.d("***PANOS", income.toString())
    }

}
