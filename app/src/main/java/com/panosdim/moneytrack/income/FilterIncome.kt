package com.panosdim.moneytrack.income

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import com.panosdim.moneytrack.DecimalDigitsInputFilter
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.network.getIncome
import kotlinx.android.synthetic.main.activity_filter_income.*
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FilterIncome : AppCompatActivity() {

    private lateinit var minDatePickerDialog: DatePickerDialog
    private lateinit var maxDatePickerDialog: DatePickerDialog

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_income)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        salaryMin.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))
        salaryMax.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        // Store list of income before filtered
        if (!mFiltersSet) {
            mIncomeList = incomeList.toList()
        }

        dateMin.setOnClickListener {
            // Use the date from the TextView
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date: Date? = try {
                df.parse(dateMin.text.toString())
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
            minDatePickerDialog = DatePickerDialog(this@FilterIncome,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        c.set(year, month, dayOfMonth, 0, 0)
                        dateMin.setText(df.format(c.time))
                    }, cYear, cMonth, cDay)
            minDatePickerDialog.show()
        }

        dateMax.setOnClickListener {
            // Use the date from the TextView
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date: Date? = try {
                df.parse(dateMax.text.toString())
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
            maxDatePickerDialog = DatePickerDialog(this@FilterIncome,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        c.set(year, month, dayOfMonth, 0, 0)
                        dateMax.setText(df.format(c.time))
                    }, cYear, cMonth, cDay)
            maxDatePickerDialog.show()
        }

        dateMin.setText(mDateMin)
        dateMax.setText(mDateMax)
        salaryMin.setText(mSalaryMin)
        salaryMax.setText(mSalaryMax)
        commentSearch.setText(mComment)

        btnSetFilters.setOnClickListener {
            if (mFiltersSet) {
                incomeList.clear()
                incomeList.addAll(mIncomeList)
            }
            val df = SimpleDateFormat("yyyy-MM-dd")

            // Validate Dates
            if (validateDates()) {
                // Min Date filter
                mDateMin = dateMin.text.toString()
                if (mDateMin.isNotEmpty()) {
                    val parsedMinDate = df.parse(mDateMin)
                    incomeList.retainAll {
                        val itDate = df.parse(it.date)
                        itDate.time >= parsedMinDate.time
                    }
                }

                // Max Date filter
                mDateMax = dateMax.text.toString()
                if (mDateMax.isNotEmpty()) {
                    val parsedMaxDate = df.parse(mDateMax)
                    incomeList.retainAll {
                        val itDate = df.parse(it.date)
                        itDate.time <= parsedMaxDate.time
                    }
                }

                // Min Salary
                mSalaryMin = salaryMin.text.toString()
                if (mSalaryMin.isNotEmpty()) {
                    incomeList.retainAll {
                        it.salary.toDouble() >= mSalaryMin.toDouble()
                    }
                }

                // Max Salary
                mSalaryMax = salaryMax.text.toString()
                if (mSalaryMax.isNotEmpty()) {
                    incomeList.retainAll {
                        it.salary.toDouble() <= mSalaryMax.toDouble()
                    }
                }

                // Comment Search
                mComment = commentSearch.text.toString()
                if (mComment.isNotEmpty()) {
                    incomeList.retainAll {
                        it.comment.contains(mComment)
                    }
                }

                mFiltersSet = mDateMin.isNotEmpty() || mDateMax.isNotEmpty() || mSalaryMin.isNotEmpty() || mSalaryMax.isNotEmpty() || mComment.isNotEmpty()

                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }

        btnClearFilters.setOnClickListener {
            mDateMin = ""
            mDateMax = ""
            mSalaryMin = ""
            mSalaryMax = ""
            mComment = ""
            mFiltersSet = false

            getIncome {
                if (it.isNotEmpty()) {
                    incomeList.clear()
                    // Convert JSON response to List<Income>
                    val resp = JSONArray(it)
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        incomeList.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                    }
                }

                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun validateDates(): Boolean {
        // Reset errors.
        dateMin.error = null
        dateMax.error = null

        // Store values.
        val minDate = dateMin.text.toString()
        val maxDate = dateMax.text.toString()

        var valid = true
        var focusView: View? = null
        val df = SimpleDateFormat("yyyy-MM-dd")

        // Check for a valid minimum date.
        if (minDate.isNotEmpty()) {
            val parsedMinDate: Date? = try {
                df.parse(minDate)
            } catch (e: ParseException) {
                null
            }
            if (parsedMinDate == null) {
                dateMin.error = getString(R.string.invalidDate)
                focusView = dateMin
                valid = false
            }
        }

        // Check for a valid maximum date.
        if (maxDate.isNotEmpty()) {
            val parsedMaxDate: Date? = try {
                df.parse(maxDate)
            } catch (e: ParseException) {
                null
            }
            if (parsedMaxDate == null) {
                dateMax.error = getString(R.string.invalidDate)
                focusView = dateMax
                valid = false
            }

            if (!valid) {
                // There was an error; don't attempt to store data and focus the first
                // form field with an error.
                focusView!!.requestFocus()
            }
        }

        return valid
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    companion object {
        private var mDateMin = ""
        private var mDateMax = ""
        private var mSalaryMin = ""
        private var mSalaryMax = ""
        private var mComment = ""
        private var mIncomeList = listOf<Income>()
        private var mFiltersSet = false
    }
}
