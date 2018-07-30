package com.panosdim.moneytrack

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_income_details.*
import kotlinx.android.synthetic.main.content_income_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class IncomeDetails : AppCompatActivity() {

    private lateinit var datePickerDialog: DatePickerDialog

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_details)
        setSupportActionBar(toolbar)

        incDate.setOnClickListener {
            // Use the date from the TextView
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date: Date? = try {
                df.parse(incDate.text.toString())
            } catch (e: ParseException) {
                null
            }
            if (date != null) {
                c.time = date
            }

            val cYear = c.get(Calendar.YEAR)
            val cMonth = c.get(Calendar.MONTH)
            val cDay = c.get(Calendar.DAY_OF_MONTH)

            // date picker dialog
            datePickerDialog = DatePickerDialog(this@IncomeDetails,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        c.set(year, month, dayOfMonth, 0, 0)
                        incDate.setText(df.format(c.time))
                    }, cYear, cMonth, cDay)
            datePickerDialog.show()
        }

        val bundle = intent.extras
        if (bundle != null) {
            val income = bundle.getParcelable<Parcelable>(INCOME_MESSAGE) as Income
            incDate.setText(income.date)
            incSalary.setText(income.salary)
            incComment.setText(income.comment)
            incDelete.visibility = View.VISIBLE
        } else {
            incDelete.visibility = View.GONE
        }
    }

}
