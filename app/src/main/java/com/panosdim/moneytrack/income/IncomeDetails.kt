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
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.network.deleteIncome
import com.panosdim.moneytrack.network.saveIncome
import kotlinx.android.synthetic.main.activity_income_details.*
import kotlinx.android.synthetic.main.content_income_details.*
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class IncomeDetails : AppCompatActivity() {

    private lateinit var datePickerDialog: DatePickerDialog
    private var income = Income(date = "", salary = "", comment = "")
    private lateinit var mCalendar: Calendar

    @SuppressLint("SimpleDateFormat")
    private val mDateFormatter = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        incSalary.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        incDate.setOnClickListener {
            // Use the date from the TextView
            mCalendar = Calendar.getInstance()
            try {
                val date = mDateFormatter.parse(incDate.text.toString())
                mCalendar.time = date
            } catch (e: ParseException) {
                mCalendar = Calendar.getInstance()
            }

            val cYear = mCalendar.get(Calendar.YEAR)
            val cMonth = mCalendar.get(Calendar.MONTH)
            val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

            // date picker dialog
            datePickerDialog = DatePickerDialog(this@IncomeDetails,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        mCalendar.set(year, month, dayOfMonth, 0, 0)
                        incDate.setText(mDateFormatter.format(mCalendar.time))
                    }, cYear, cMonth, cDay)
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            if (income.id != null) {
                deleteIncome({ inc ->
                    val res = JSONObject(inc)
                    if (res.getBoolean("success")) {
                        incomeList.remove(income)
                        if (FilterIncome.isFilterSet()) {
                            val intent = Intent(this, FilterIncome::class.java)
                            val bundle = Bundle()
                            bundle.putParcelable(INCOME_MESSAGE, income)
                            bundle.putString(OPERATION_MESSAGE, Operations.FILTER_DELETE_INCOME.name)
                            intent.putExtras(bundle)
                            startActivityForResult(intent, Operations.FILTER_DELETE_INCOME.code)
                        } else {
                            val returnIntent = Intent()
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    }

                    Toast.makeText(this, res.getString("message"),
                            Toast.LENGTH_LONG).show()
                }, income.toJson())
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
        if (date.isEmpty()) {
            incDate.error = getString(R.string.error_field_required)
            focusView = incDate
            cancel = true
        } else {
            try {
                mDateFormatter.parse(date)
            } catch (e: ParseException) {
                incDate.error = getString(R.string.invalidDate)
                focusView = incDate
                cancel = true
            }
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

            saveIncome({
                val res = JSONObject(it)
                if (res.getBoolean("success")) {
                    if (income.id == null) {
                        income.id = res.getJSONObject("data").getString("id")
                        incomeList.add(income)
                    } else {
                        val index = incomeList.indexOfFirst { inc -> inc.id == income.id }
                        incomeList[index] = income
                    }
                    if (FilterIncome.isFilterSet()) {
                        val intent = Intent(this, FilterIncome::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable(INCOME_MESSAGE, income)
                        bundle.putString(OPERATION_MESSAGE, Operations.FILTER_ADD_INCOME.name)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, Operations.FILTER_ADD_INCOME.code)
                    } else {
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }

                Toast.makeText(this, res.getString("message"),
                        Toast.LENGTH_LONG).show()
            }, income.toJson())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Operations.FILTER_ADD_INCOME.code || requestCode == Operations.FILTER_DELETE_INCOME.code) {
                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}
