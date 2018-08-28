package com.panosdim.moneytrack.expense

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.category.Category
import com.panosdim.moneytrack.network.deleteExpense
import com.panosdim.moneytrack.network.saveExpense
import kotlinx.android.synthetic.main.activity_expense_details.*
import kotlinx.android.synthetic.main.content_expense_details.*
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetails : AppCompatActivity() {

    private lateinit var datePickerDialog: DatePickerDialog
    private var expense = Expense(date = "", amount = "", category = "", comment = "")
    private lateinit var mCalendar: Calendar

    @SuppressLint("SimpleDateFormat")
    private val mDateFormatter = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Set decimal filter to amount
        expAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        // Initialize category spinner data
        val spinnerData = ArrayAdapter<Category>(this, android.R.layout.simple_spinner_dropdown_item, categoriesList)
        expCategory.adapter = spinnerData

        expDate.setOnClickListener {
            // Use the date from the TextView
            mCalendar = Calendar.getInstance()
            try {
                val date = mDateFormatter.parse(expDate.text.toString())
                mCalendar.time = date
            } catch (e: ParseException) {
                mCalendar = Calendar.getInstance()
            }

            val cYear = mCalendar.get(Calendar.YEAR)
            val cMonth = mCalendar.get(Calendar.MONTH)
            val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

            // date picker dialog
            datePickerDialog = DatePickerDialog(this@ExpenseDetails,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        mCalendar.set(year, month, dayOfMonth, 0, 0)
                        expDate.setText(mDateFormatter.format(mCalendar.time))
                    }, cYear, cMonth, cDay)
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            if (expense.id != null) {
                deleteExpense({
                    val res = JSONObject(it)
                    if (res.getString("status") != "error") {
                        expensesList.remove(expense)
                        if (FilterExpenses.isFilterSet()) {
                            val intent = Intent(this, FilterExpenses::class.java)
                            val bundle = Bundle()
                            bundle.putParcelable(EXPENSE_MESSAGE, expense)
                            bundle.putString(OPERATION_MESSAGE, Operations.FILTER_DELETE_EXPENSE.name)
                            intent.putExtras(bundle)
                            startActivityForResult(intent, Operations.FILTER_DELETE_EXPENSE.code)
                        } else {
                            val returnIntent = Intent()
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    }

                    Toast.makeText(this, res.getString("message"),
                            Toast.LENGTH_LONG).show()
                }, expense.toJson())
            } else {
                Toast.makeText(this, "Expense ID was not found",
                        Toast.LENGTH_LONG).show()
            }
        }

        val bundle = intent.extras
        if (bundle != null) {
            expense = bundle.getParcelable<Parcelable>(EXPENSE_MESSAGE) as Expense
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }

        expDate.setText(expense.date)
        expAmount.setText(expense.amount)
        val selectedItem = spinnerData.getPosition(categoriesList.find {
            it.category == expense.category
        })
        expCategory.setSelection(selectedItem)
        expComment.setText(expense.comment)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun validateInputs() {
        // Reset errors.
        expDate.error = null
        expAmount.error = null

        // Store values.
        val date = expDate.text.toString()
        val amount = expAmount.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid date.
        if (date.isEmpty()) {
            expDate.error = getString(R.string.error_field_required)
            focusView = expDate
            cancel = true
        } else {
            try {
                mDateFormatter.parse(date)
            } catch (e: ParseException) {
                expDate.error = getString(R.string.invalidDate)
                focusView = expDate
                cancel = true
            }
        }

        // Check for a valid salary.
        if (amount.isEmpty()) {
            expAmount.error = getString(R.string.error_field_required)
            focusView = expAmount
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            expense.date = expDate.text.toString()
            expense.amount = expAmount.text.toString()
            expense.category = expCategory.selectedItem.toString()
            expense.comment = expComment.text.toString()

            saveExpense({
                val res = JSONObject(it)
                if (res.getString("status") != "error") {
                    if (expense.id == null) {
                        expense.id = res.getString("id")
                        expensesList.add(expense)
                    } else {
                        val index = expensesList.indexOfFirst { it.id == expense.id }
                        expensesList[index] = expense
                    }
                    if (FilterExpenses.isFilterSet()) {
                        val intent = Intent(this, FilterExpenses::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable(EXPENSE_MESSAGE, expense)
                        bundle.putString(OPERATION_MESSAGE, Operations.FILTER_ADD_EXPENSE.name)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, Operations.FILTER_ADD_EXPENSE.code)
                    } else {
                        val returnIntent = Intent()
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }
                }

                Toast.makeText(this, res.getString("message"),
                        Toast.LENGTH_LONG).show()
            }, expense.toJson())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Operations.FILTER_ADD_EXPENSE.code || requestCode == Operations.FILTER_DELETE_EXPENSE.code) {
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
