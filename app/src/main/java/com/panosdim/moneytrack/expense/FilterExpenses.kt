package com.panosdim.moneytrack.expense

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
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

    @SuppressLint("SimpleDateFormat")
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
        if (spinnerData.count == categoriesList.size ) {
            spinnerData.insert(Category(category = "Select Category"), 0)
        }
        expCategory.adapter = spinnerData

        // Store list of expenses before filtered
        if (!mFiltersSet) {
            mExpensesList = expensesList.toList()
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
            minDatePickerDialog = DatePickerDialog(this@FilterExpenses,
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
            maxDatePickerDialog = DatePickerDialog(this@FilterExpenses,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        c.set(year, month, dayOfMonth, 0, 0)
                        dateMax.setText(df.format(c.time))
                    }, cYear, cMonth, cDay)
            maxDatePickerDialog.show()
        }

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
            val df = SimpleDateFormat("yyyy-MM-dd")

            // Validate Dates
            if (validateDates()) {
                // Min Date filter
                mDateMin = dateMin.text.toString()
                if (mDateMin.isNotEmpty()) {
                    val parsedMinDate = df.parse(mDateMin)
                    expensesList.retainAll {
                        val itDate = df.parse(it.date)
                        itDate.time >= parsedMinDate.time
                    }
                }

                // Max Date filter
                mDateMax = dateMax.text.toString()
                if (mDateMax.isNotEmpty()) {
                    val parsedMaxDate = df.parse(mDateMax)
                    expensesList.retainAll {
                        val itDate = df.parse(it.date)
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
            mDateMin = ""
            mDateMax = ""
            mExpenseMin = ""
            mExpenseMax = ""
            mCategory = ""
            mComment = ""
            expCategory.setSelection(0)
            mFiltersSet = false

            getExpenses {
                if (it.isNotEmpty()) {
                    expensesList.clear()
                    // Convert JSON response to List<Income>
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
        private var mExpenseMin = ""
        private var mExpenseMax = ""
        private var mCategory = ""
        private var mComment = ""
        private var mExpensesList = listOf<Expense>()
        private var mFiltersSet = false
    }
}
