package com.panosdim.moneytrack

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.panosdim.moneytrack.network.PutJsonData
import kotlinx.android.synthetic.main.activity_expense_details.*
import kotlinx.android.synthetic.main.content_expense_details.*
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ExpenseDetails : AppCompatActivity() {

    private lateinit var datePickerDialog: DatePickerDialog
    private var expense: Expense = Expense(date = "", amount = "", category = "", comment = "")

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Set decimal filter to amount
        expAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        // Initialize category spinner data
        val spinnerData = ArrayAdapter<Category>(this, android.R.layout.simple_spinner_dropdown_item, categories)
        expCategory.adapter = spinnerData;

        expDate.setOnClickListener {
            // Use the date from the TextView
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("yyyy-MM-dd")
            val date: Date? = try {
                df.parse(expDate.text.toString())
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
            datePickerDialog = DatePickerDialog(this@ExpenseDetails,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        c.set(year, month, dayOfMonth, 0, 0)
                        expDate.setText(df.format(c.time))
                    }, cYear, cMonth, cDay)
            datePickerDialog.show()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            if (expense.id != null) {
                PutJsonData(::deleteExpenseTask, "php/delete_expense.php").execute(expense.toJson())
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
        val selectedItem = spinnerData.getPosition(categories.find {
            it.category.equals(expense.category)
        })
        expCategory.setSelection(selectedItem)
        expComment.setText(expense.comment)
    }

    private fun deleteExpenseTask(result: String) {
        val res = JSONObject(result)
        if (res.getString("status") != "error") {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(TAB_NUMBER_MESSAGE, 1)
            startActivity(intent)
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
        expDate.error = null
        expAmount.error = null

        // Store values.
        val date = expDate.text.toString()
        val amount = expAmount.text.toString()

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
            expDate.error = getString(R.string.error_field_required)
            focusView = expDate
            cancel = true
        }
        if (parsedDate == null) {
            expDate.error = getString(R.string.invalidDate)
            focusView = expDate
            cancel = true
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

            PutJsonData(::saveExpenseTask, "php/save_expense.php").execute(expense.toJson())
        }
    }

    private fun saveExpenseTask(result: String) {
        val res = JSONObject(result)
        if (res.getString("status") != "error") {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(TAB_NUMBER_MESSAGE, 1)
            startActivity(intent)
        }

        Toast.makeText(this, res.getString("message"),
                Toast.LENGTH_LONG).show()
    }
}
