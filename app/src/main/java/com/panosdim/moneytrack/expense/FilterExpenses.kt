package com.panosdim.moneytrack.expense

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import com.panosdim.moneytrack.DecimalDigitsInputFilter
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.category.Category
import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.network.getExpenses
import kotlinx.android.synthetic.main.activity_filter_expenses.*
import org.json.JSONArray
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class FilterExpenses : AppCompatActivity() {

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
        setContentView(R.layout.activity_filter_expenses)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        expenseMin.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))
        expenseMax.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        // Initialize category spinner data
        val spinnerData = ArrayAdapter<Category>(this, android.R.layout.simple_spinner_dropdown_item, categoriesList.toList())
        // Add Select Category as first element only the first time
        if (spinnerData.count == categoriesList.size) {
            spinnerData.insert(Category(category = "Select Category"), 0)
        }
        expCategory.adapter = spinnerData

        // Store list of expenses before filtered
        if (!mFiltersSet) {
            mExpensesList = expensesList.toList()
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
            minDatePickerDialog = DatePickerDialog(this@FilterExpenses,
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
            maxDatePickerDialog = DatePickerDialog(this@FilterExpenses,
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
        expenseMin.setText(mExpenseMin)
        expenseMax.setText(mExpenseMax)
        val selectedItem = spinnerData.getPosition(categoriesList.find {
            it.category == mCategory
        })
        expCategory.setSelection(selectedItem)
        commentSearch.setText(mComment)

        btnSetFilters.setOnClickListener {
            if (mFiltersSet) {
                expensesList.clear()
                expensesList.addAll(mExpensesList)
            }

            // Validate Dates
            if (validateDates()) {
                // Min Date filter
                mDateMin = dateMin.text.toString()
                if (mDateMin.isNotEmpty()) {
                    val parsedMinDate = mDateFormatter.parse(mDateMin)
                    expensesList.retainAll {
                        val itDate = mDateFormatter.parse(it.date)
                        itDate.time >= parsedMinDate.time
                    }
                }

                // Max Date filter
                mDateMax = dateMax.text.toString()
                if (mDateMax.isNotEmpty()) {
                    val parsedMaxDate = mDateFormatter.parse(mDateMax)
                    expensesList.retainAll {
                        val itDate = mDateFormatter.parse(it.date)
                        itDate.time <= parsedMaxDate.time
                    }
                }

                // Min Salary
                mExpenseMin = expenseMin.text.toString()
                if (mExpenseMin.isNotEmpty()) {
                    expensesList.retainAll {
                        it.amount.toDouble() >= mExpenseMin.toDouble()
                    }
                }

                // Max Salary
                mExpenseMax = expenseMax.text.toString()
                if (mExpenseMax.isNotEmpty()) {
                    expensesList.retainAll {
                        it.amount.toDouble() <= mExpenseMax.toDouble()
                    }
                }

                // Category
                mCategory = expCategory.selectedItem.toString()
                if (expCategory.selectedItemPosition != 0) {
                    expensesList.retainAll {
                        it.category == mCategory
                    }
                }

                // Comment Search
                mComment = commentSearch.text.toString()
                if (mComment.isNotEmpty()) {
                    expensesList.retainAll {
                        it.comment.contains(mComment)
                    }
                }

                mFiltersSet = mDateMin.isNotEmpty() || mDateMax.isNotEmpty() || mExpenseMin.isNotEmpty() || mExpenseMax.isNotEmpty() || expCategory.selectedItemPosition != 0 || mComment.isNotEmpty()

                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }

        btnClearFilters.setOnClickListener {
            expCategory.setSelection(0)
            mSetMinDate = false
            mSetMaxDate = false

            clearFilters()

            getExpenses {
                if (it.isNotEmpty()) {
                    expensesList.clear()
                    // Convert JSON response to List<Expense>
                    val resp = JSONArray(it)
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        expensesList.add(Expense(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("category"), item.getString("comment")))
                    }
                }

                val returnIntent = Intent()
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
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
        private var mExpenseMin = ""
        private var mExpenseMax = ""
        private var mCategory = ""
        private var mComment = ""
        private var mExpensesList = listOf<Expense>()
        var mFiltersSet = false

        fun clearFilters() {
            mDateMin = ""
            mDateMax = ""
            mExpenseMin = ""
            mExpenseMax = ""
            mCategory = ""
            mComment = ""
            mFiltersSet = false
        }
    }
}
