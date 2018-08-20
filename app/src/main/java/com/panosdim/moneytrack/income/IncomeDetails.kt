package com.panosdim.moneytrack.income

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import com.panosdim.moneytrack.DecimalDigitsInputFilter
import com.panosdim.moneytrack.INCOME_MESSAGE
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.network.PutJsonData
import kotlinx.android.synthetic.main.activity_income_details.*
import kotlinx.android.synthetic.main.content_income_details.*
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class IncomeDetails : AppCompatActivity() {

    private lateinit var datePickerDialog: DatePickerDialog
    private var income = Income(date = "", salary = "", comment = "")

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        incSalary.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

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

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            if (income.id != null) {
                PutJsonData(::deleteIncomeTask, "php/delete_income.php").execute(income.toJson())
            } else {
                Toast.makeText(this, "Income ID was not found",
                        Toast.LENGTH_LONG).show()
            }
        }

        val bundle = intent.extras
        if (bundle != null) {
            income = bundle.getParcelable<Parcelable>(INCOME_MESSAGE) as Income
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }

        incDate.setText(income.date)
        incSalary.setText(income.salary)
        incComment.setText(income.comment)
    }

    private fun deleteIncomeTask(result: String) {
        val res = JSONObject(result)
        if (res.getString("status") != "error") {
            val returnIntent = Intent()
            incomeList.remove(income)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        Toast.makeText(this, res.getString("message"),
                Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @SuppressLint("SimpleDateFormat")
    private fun validateInputs() {
        // Reset errors.
        incDate.error = null
        incSalary.error = null

        // Store values.
        val date = incDate.text.toString()
        val salary = incSalary.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid date.
        val df = SimpleDateFormat("yyyy-MM-dd")
        val parsedDate: Date? = try {
            df.parse(date)
        } catch (e: ParseException) {
            null
        }
        if (date.isEmpty()) {
            incDate.error = getString(R.string.error_field_required)
            focusView = incDate
            cancel = true
        }
        if (parsedDate == null) {
            incDate.error = getString(R.string.invalidDate)
            focusView = incDate
            cancel = true
        }

        // Check for a valid salary.
        if (salary.isEmpty()) {
            incSalary.error = getString(R.string.error_field_required)
            focusView = incSalary
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            income.date = incDate.text.toString()
            income.salary = incSalary.text.toString()
            income.comment = incComment.text.toString()

            PutJsonData(::saveIncomeTask, "php/save_income.php").execute(income.toJson())
        }
    }

    private fun saveIncomeTask(result: String) {
        val res = JSONObject(result)
        if (res.getString("status") != "error") {
            if (income.id == null) {
                income.id = res.getString("id")
                incomeList.add(income)
            } else {
                val index = incomeList.indexOfFirst { it.id == income.id }
                incomeList[index] = income
            }
            incomeList.sortByDescending { it.date }
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        Toast.makeText(this, res.getString("message"),
                Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}
