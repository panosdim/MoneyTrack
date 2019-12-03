package com.panosdim.moneytrack.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.ExpensesFilters.isFiltersSet
import com.panosdim.moneytrack.model.ExpensesFilters.unfilteredExpensesList
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.rest.requests.ExpenseRequest
import kotlinx.android.synthetic.main.dialog_expense.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class ExpenseDialog(
    private var _context: Context,
    private var listener: RefreshView,
    private var expense: Expense? = null
) :
    Dialog(_context) {

    private lateinit var datePickerDialog: DatePickerDialog
    private var dateSelected = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("E dd-MM-yyyy")
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_expense)
        val windowProps = window?.attributes

        windowProps?.gravity = Gravity.BOTTOM
        windowProps?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = windowProps
        this.setCanceledOnTouchOutside(false)

        // Set decimal filter to amount
        tvAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(5, 2))

        // Initialize category spinner data
        val spinnerData = ArrayAdapter<Category>(
            _context,
            android.R.layout.simple_spinner_dropdown_item,
            categoriesList
        )
        spCategory.adapter = spinnerData

        setupListeners()

        expense?.let {
            tvTitle.text = _context.getString(R.string.edit_expense)
            dateSelected = try {
                LocalDate.parse(it.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            tvDate.setText(dateSelected.format(dateFormatter))
            tvAmount.setText(it.amount.toString())
            tvComment.setText(it.comment)
            val selectedItem = spinnerData.getPosition(categoriesList.find { (id) ->
                id == it.category
            })
            spCategory.setSelection(selectedItem)
        } ?: kotlin.run {
            btnDelete.visibility = View.GONE
            tvDate.setText(dateSelected.format(dateFormatter))
        }
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            this.hide()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            expense?.let {
                scope.launch {
                    prgIndicator.visibility = View.VISIBLE
                    btnDelete.isEnabled = false
                    try {
                        val response = repository.deleteExpense(it.id!!)
                        when (response.code()) {
                            204 -> {
                                expensesList.remove(it)
                                if (isFiltersSet) {
                                    unfilteredExpensesList.remove(it)
                                }
                                listener.refreshView()
                                this@ExpenseDialog.hide()
                            }
                            404 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting expense. Expense not found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                            403 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting expense. Expense not belong to you.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                        }
                    } catch (ex: HttpException) {
                        Toast.makeText(
                            _context,
                            "Error deleting expense.",
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
            saveExpense()
        }
    }

    private fun saveExpense() {
        prgIndicator.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val category = spCategory.selectedItem as Category
        val data = ExpenseRequest(
            dateSelected.toString(),
            tvAmount.text.toString(),
            tvComment.text.toString(),
            category.id.toString()
        )

        expense?.let {
            // Check if we change something in the object
            if (it.date == dateSelected.toString() && it.amount == tvAmount.text.toString().toFloat() && it.comment == tvComment.text.toString() && it.category == category.id) {
                this@ExpenseDialog.hide()
                return
            }

            // Update expense
            scope.launch {
                try {
                    val response = repository.updateExpense(it.id!!, data)
                    var index = expensesList.indexOfFirst { (id) -> id == response.data.id }
                    expensesList[index] = response.data
                    if (isFiltersSet) {
                        index =
                            unfilteredExpensesList.indexOfFirst { (id) -> id == response.data.id }
                        unfilteredExpensesList[index] = response.data
                    }
                    listener.refreshView()
                    this@ExpenseDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error updating expense.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        } ?: kotlin.run {
            // Save expense
            scope.launch {
                try {
                    val response = repository.createNewExpense(data)
                    expensesList.add(response.data)
                    if (isFiltersSet) {
                        unfilteredExpensesList.add(response.data)
                    }
                    listener.refreshView()
                    this@ExpenseDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error creating new expense.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }
}