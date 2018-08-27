package com.panosdim.moneytrack.income

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import com.panosdim.moneytrack.DecimalDigitsInputFilter
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.network.INCOME_MESSAGE
import kotlinx.android.synthetic.main.activity_filter_income.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class FilterIncome : AppCompatActivity() {

    private lateinit var minDatePickerDialog: DatePickerDialog
    private lateinit var maxDatePickerDialog: DatePickerDialog

    private var mMinDate = Calendar.getInstance()
    private var mMaxDate = Calendar.getInstance()
    private var mSetMinDate = false
    private var mSetMaxDate = false
    private lateinit var mCalendar: Calendar

    @SuppressLint("SimpleDateFormat")
    private val mDateFormatter = SimpleDateFormat("yyyy-MM-dd")

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
            mIncomeList.addAll(incomeList)
        }

        dateMin.setOnClickListener {
            // Use the date from the TextView
            mCalendar = Calendar.getInstance()
            if (dateMin.text.isNotEmpty()) {
                try {
                    val date = mDateFormatter.parse(dateMin.text.toString())
                    mCalendar.time = date
                } catch (e: ParseException) {
                    mCalendar = Calendar.getInstance()
                }
            }

            val cYear = mCalendar.get(Calendar.YEAR)
            val cMonth = mCalendar.get(Calendar.MONTH)
            val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

            // date picker dialog
            minDatePickerDialog = DatePickerDialog(this@FilterIncome,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        mCalendar.set(year, month, dayOfMonth, 0, 0)
                        dateMin.setText(mDateFormatter.format(mCalendar.time))
                        mMinDate.set(year, month, dayOfMonth, 0, 0)
                        mSetMinDate = true
                    }, cYear, cMonth, cDay)
            if (mSetMaxDate) {
                minDatePickerDialog.datePicker.maxDate = mMaxDate.timeInMillis
            }
            minDatePickerDialog.show()
        }

        dateMax.setOnClickListener {
            mCalendar = Calendar.getInstance()
            if (dateMax.text.isNotEmpty()) {
                try {
                    val date = mDateFormatter.parse(dateMax.text.toString())
                    mCalendar.time = date
                } catch (e: ParseException) {
                    mCalendar = Calendar.getInstance()
                }
            }

            val cYear = mCalendar.get(Calendar.YEAR)
            val cMonth = mCalendar.get(Calendar.MONTH)
            val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

            // date picker dialog
            maxDatePickerDialog = DatePickerDialog(this@FilterIncome,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        mCalendar.set(year, month, dayOfMonth, 0, 0)
                        dateMax.setText(mDateFormatter.format(mCalendar.time))
                        mMaxDate.set(year, month, dayOfMonth, 0, 0)
                        mSetMaxDate = true
                    }, cYear, cMonth, cDay)
            if (mSetMinDate) {
                maxDatePickerDialog.datePicker.minDate = mMinDate.timeInMillis
            }
            maxDatePickerDialog.show()
        }

        dateMin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val date = mDateFormatter.parse(s.toString())
                    mMinDate.time = date
                    mSetMinDate = true
                } catch (e: ParseException) {
                    mSetMinDate = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        dateMax.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                try {
                    val date = mDateFormatter.parse(s.toString())
                    mMaxDate.time = date
                    mSetMaxDate = true
                } catch (e: ParseException) {
                    mSetMaxDate = false
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

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

            // Validate Dates
            if (validateDates()) {
                // Min Date filter
                mDateMin = dateMin.text.toString()
                if (mDateMin.isNotEmpty()) {
                    val parsedMinDate = mDateFormatter.parse(mDateMin)
                    incomeList.retainAll {
                        val itDate = mDateFormatter.parse(it.date)
                        itDate.time >= parsedMinDate.time
                    }
                }

                // Max Date filter
                mDateMax = dateMax.text.toString()
                if (mDateMax.isNotEmpty()) {
                    val parsedMaxDate = mDateFormatter.parse(mDateMax)
                    incomeList.retainAll {
                        val itDate = mDateFormatter.parse(it.date)
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
            mSetMinDate = false
            mSetMaxDate = false

            mDateMin = ""
            mDateMax = ""
            mSalaryMin = ""
            mSalaryMax = ""
            mComment = ""

            if (mFiltersSet) {
                mFiltersSet = false
                incomeList.clear()
                incomeList.addAll(mIncomeList)
            }

            mIncomeList.clear()

            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        val bundle = intent.extras
        if (bundle != null) {
            val income = bundle.getParcelable<Parcelable>(INCOME_MESSAGE) as Income
            mIncomeList.add(income)
            btnSetFilters.performClick()
        }
    }

    private fun validateDates(): Boolean {
        // Reset errors.
        dateMin.error = null
        dateMax.error = null

        // Store values.
        val minDate = dateMin.text.toString()
        val maxDate = dateMax.text.toString()
        var parsedMinDate: Date? = null
        var parsedMaxDate: Date? = null

        var valid = true
        var focusView: View? = null

        // Check for a valid minimum date.
        if (minDate.isNotEmpty()) {
            parsedMinDate = try {
                mDateFormatter.parse(minDate)
            } catch (e: ParseException) {
                dateMin.error = getString(R.string.invalidDate)
                focusView = dateMin
                valid = false
                null
            }
        }

        // Check for a valid maximum date.
        if (maxDate.isNotEmpty()) {
            parsedMaxDate = try {
                mDateFormatter.parse(maxDate)
            } catch (e: ParseException) {
                dateMax.error = getString(R.string.invalidDate)
                focusView = dateMax
                valid = false
                null
            }
        }

        // Check that minDate is not greater than maxDate
        if (maxDate.isNotEmpty() && minDate.isNotEmpty()) {
            if (parsedMaxDate != null && parsedMinDate != null) {
                if (parsedMinDate.time > parsedMaxDate.time) {
                    dateMax.error = getString(R.string.minDateGraterMaxDate)
                    dateMin.error = getString(R.string.minDateGraterMaxDate)
                    focusView = dateMin
                    valid = false
                }
            }

        }

        if (!valid) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
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
        private var mIncomeList = mutableListOf<Income>()
        var mFiltersSet = false
    }
}
