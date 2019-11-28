package com.panosdim.moneytrack.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.panosdim.moneytrack.DecimalDigitsInputFilter
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import com.panosdim.moneytrack.rest.requests.IncomeRequest
import kotlinx.android.synthetic.main.dialog_income.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class IncomeDialog(
    private var _context: Context,
    private var listener: RefreshView,
    private var income: Income? = null
) :
    Dialog(_context) {

    private lateinit var datePickerDialog: DatePickerDialog
    private var dateSelected = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_income)
        val windowProps = window?.attributes

        windowProps?.gravity = Gravity.BOTTOM
        windowProps?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = windowProps
        this.setCanceledOnTouchOutside(false)

        // Set decimal filter to amount
        tvAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        btnCancel.setOnClickListener {
            this.hide()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            this.hide()
            income?.let {
                scope.launch {
                    prgIndicator.visibility = View.VISIBLE
                    btnDelete.isEnabled = false
                    try {
                        val response = repository.deleteIncome(it.id!!)
                        when (response.code()) {
                            204 -> {
                                incomeList.remove(it)
                                listener.refreshView()
                                this@IncomeDialog.hide()
                            }
                            404 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting income. Income not found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                            403 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting income. Income not belong to you.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                        }
                    } catch (ex: HttpException) {
                        Toast.makeText(
                            _context,
                            "Error deleting income.",
                            Toast.LENGTH_LONG
                        ).show()
                        prgIndicator.visibility = View.GONE
                        btnDelete.isEnabled = true
                    }
                }
            }
        }

        tvDate.setOnClickListener {
            // Use the date from the TextView
            val date: LocalDate = try {
                LocalDate.parse(tvDate.text.toString())
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }

            val cYear = date.year
            val cMonth = date.monthValue - 1
            val cDay = date.dayOfMonth

            // date picker dialog
            datePickerDialog = DatePickerDialog(
                _context,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    // set day of month , month and year value in the edit text
                    dateSelected = LocalDate.of(year, month + 1, dayOfMonth)
                    tvDate.setText(dateSelected.format(dateFormatter))
                }, cYear, cMonth, cDay
            )
            datePickerDialog.show()
        }

        income?.let {
            tvTitle.text = _context.getString(R.string.edit_income)
            dateSelected = try {
                LocalDate.parse(it.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            tvDate.setText(dateSelected.format(dateFormatter))
            tvAmount.setText(it.amount.toString())
            tvComment.setText(it.comment)
        } ?: kotlin.run {
            btnDelete.visibility = View.GONE
            tvDate.setText(dateSelected.format(dateFormatter))
        }
    }

    private fun validateInputs() {
        // Reset errors.
        tvDate.error = null
        tvAmount.error = null

        // Store values.
        val date = tvDate.text.toString()
        val salary = tvAmount.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid date.
        if (date.isEmpty()) {
            tvDate.error = _context.getString(R.string.error_field_required)
            focusView = tvDate
            cancel = true
        }

        // Check for a valid salary.
        if (salary.isEmpty()) {
            tvAmount.error = _context.getString(R.string.error_field_required)
            focusView = tvAmount
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            saveIncome()
        }
    }

    private fun saveIncome() {
        prgIndicator.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val data = IncomeRequest(
            dateSelected.toString(),
            tvAmount.text.toString(),
            tvComment.text.toString()
        )

        income?.let {
            // Check if we change something in the object
            if (it.date == dateSelected.toString() && it.amount == tvAmount.text.toString().toFloat() && it.comment == tvComment.text.toString()) {
                this@IncomeDialog.hide()
                return
            }

            // Update income
            scope.launch {
                try {
                    val response = repository.updateIncome(it.id!!, data)
                    val index = incomeList.indexOfFirst { (id) -> id == response.data.id }
                    incomeList[index] = response.data
                    listener.refreshView()
                    this@IncomeDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error updating income.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        } ?: kotlin.run {
            // Save income
            scope.launch {
                try {
                    val response = repository.createNewIncome(data)
                    incomeList.add(response.data)
                    listener.refreshView()
                    this@IncomeDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error creating new income.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }
}