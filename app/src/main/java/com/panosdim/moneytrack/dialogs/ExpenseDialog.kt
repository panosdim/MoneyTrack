package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import kotlinx.android.synthetic.main.dialog_expense.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ExpenseDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private var expense: Expense? = null
    private var categoryId: Int? = null
    private val textWatcher = generateTextWatcher(::validateForm)
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val expenseDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E dd-MM-yyyy")
    private val sqlDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var dateSelected: LocalDate = LocalDate.now()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_expense, container, false)

        dialogView.expenseAmount.filters = arrayOf<InputFilter>(
                DecimalDigitsInputFilter(
                        5,
                        2
                )
        )

        dialogView.expenseComment.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                expense?.let {
                    updateExpense(it)
                } ?: kotlin.run {
                    saveExpense()
                }
            }
            false
        }

        dialogView.expenseDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.datePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(dateSelected.toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            builder.setSelection(dateSelected.toEpochMilli())
            builder.setTitleText("Select Date")

            val picker: MaterialDatePicker<Long> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                dateSelected = fromEpochMilli(selection)
                dialogView.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            }

            picker.show(childFragmentManager, picker.toString())
        }

        dialogView.saveExpense.setOnClickListener {
            dialogView.prgIndicator.visibility = View.VISIBLE
            dialogView.saveExpense.isEnabled = false
            dialogView.deleteExpense.isEnabled = false

            expense?.let {
                updateExpense(it)
            } ?: kotlin.run {
                saveExpense()
            }
        }

        dialogView.deleteExpense.setOnClickListener {
            deleteExpense()
        }

        viewModel.categories.value?.let { list ->
            val data = list.toMutableList()
            data.sortByDescending { it.count }
            val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.list_item,
                    data
            )
            dialogView.expenseCategory.setAdapter(adapter)
        }
        dialogView.expenseCategory.setOnItemClickListener { parent, _, position, _ ->
            categoryId = (parent.getItemAtPosition(position) as Category).id
            validateForm()
        }
        requireDialog().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return dialogView
    }

    override fun onPause() {
        super.onPause()
        // Workaround as there is a bug in AutoCompleteView filter setting
        dialogView.expenseCategory.setText("", false)
        dateSelected = LocalDate.now()
        categoryId = null
    }

    private fun updateExpense(expense: Expense) {
        // Check if we change something in the object
        if (expense.date == dateSelected.format(sqlDateFormatter) &&
                expense.amount == dialogView.expenseAmount.text.toString().toFloat() &&
                expense.category == categoryId &&
                expense.comment == dialogView.expenseComment.text.toString()
        ) {
            dismiss()
        } else {
            // Update Expense
            expense.date = dateSelected.format(sqlDateFormatter)
            expense.amount = dialogView.expenseAmount.text.toString().toFloat()
            expense.category = categoryId!!
            expense.comment = dialogView.expenseComment.text.toString()

            viewModel.updateExpense(expense).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteExpense.isEnabled = true
                            dialogView.saveExpense.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteExpense.isEnabled = true
                            dialogView.saveExpense.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteExpense.isEnabled = false
                            dialogView.saveExpense.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun deleteExpense() {
        expense?.let {
            viewModel.removeExpense(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteExpense.isEnabled = true
                            dialogView.saveExpense.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteExpense.isEnabled = true
                            dialogView.saveExpense.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteExpense.isEnabled = false
                            dialogView.saveExpense.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveExpense() {
        val newExpense = Expense(
                null,
                dateSelected.format(sqlDateFormatter),
                dialogView.expenseAmount.text.toString().toFloat(),
                categoryId!!,
                dialogView.expenseComment.text.toString()
        )

        viewModel.addExpense(newExpense).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dismiss()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteExpense.isEnabled = true
                        dialogView.saveExpense.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                        ).show()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteExpense.isEnabled = true
                        dialogView.saveExpense.isEnabled = true
                    }
                    is Resource.Loading -> {
                        dialogView.prgIndicator.visibility = View.VISIBLE
                        dialogView.deleteExpense.isEnabled = false
                        dialogView.saveExpense.isEnabled = false
                    }
                }
            }
        }
    }

    private fun validateForm() {
        val expenseDate = dialogView.expenseDate
        val expenseAmount = dialogView.expenseAmount
        val saveExpense = dialogView.saveExpense
        saveExpense.isEnabled = true
        expenseDate.error = null
        expenseAmount.error = null
        dialogView.expenseCategory.error = null

        // Store values.
        val date = expenseDate.text.toString()
        val amount = expenseAmount.text.toString()

        // Check for a valid date.
        if (date.isEmpty()) {
            expenseDate.error = getString(R.string.error_field_required)
            saveExpense.isEnabled = false
        }

        // Check for a valid amount.
        if (amount.isEmpty()) {
            expenseAmount.error = getString(R.string.error_field_required)
            saveExpense.isEnabled = false
        }

        // Check if category is selected
        if (categoryId == null) {
            dialogView.expenseCategory.error = getString(R.string.error_field_required)
            saveExpense.isEnabled = false
        }
    }

    fun showForm(expItem: Expense?) {
        dialogView.prgIndicator.visibility = View.GONE
        dialogView.saveExpense.isEnabled = true
        dialogView.deleteExpense.isEnabled = true

        dialogView.expenseDate.removeTextChangedListener(textWatcher)
        dialogView.expenseDate.error = null

        dialogView.expenseAmount.removeTextChangedListener(textWatcher)
        dialogView.expenseAmount.error = null

        dialogView.expenseCategory.removeTextChangedListener(textWatcher)
        dialogView.expenseCategory.error = null

        dialogView.expenseComment.removeTextChangedListener(textWatcher)
        dialogView.expenseComment.error = null

        expense = expItem
        if (expItem == null) {
            dialogView.expenseDate.addTextChangedListener(textWatcher)
            dialogView.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            dialogView.expenseAmount.addTextChangedListener(textWatcher)
            dialogView.expenseAmount.setText("")
            dialogView.expenseCategory.addTextChangedListener(textWatcher)
            dialogView.expenseCategory.setText("")
            dialogView.expenseComment.addTextChangedListener(textWatcher)
            dialogView.expenseComment.setText("")
            dialogView.deleteExpense.visibility = View.GONE
            dialogView.saveExpense.setText(R.string.save)
        } else {
            dateSelected = try {
                LocalDate.parse(expItem.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            categoryId = expItem.category
            dialogView.expenseDate.setText(dateSelected.toShowDateFormat(expenseDateFormatter))
            dialogView.expenseAmount.setText(expItem.amount.toString())
            dialogView.expenseCategory.setText(getCategoryName(expItem.category, viewModel.categories), false)
            dialogView.expenseComment.setText(expItem.comment)
            dialogView.deleteExpense.visibility = View.VISIBLE
            dialogView.saveExpense.setText(R.string.update)
            dialogView.expenseDate.addTextChangedListener(textWatcher)
            dialogView.expenseAmount.addTextChangedListener(textWatcher)
            dialogView.expenseComment.addTextChangedListener(textWatcher)
        }
    }

    private fun isFormValid(): Boolean {
        return dialogView.expenseDate.error == null &&
                dialogView.expenseAmount.error == null &&
                dialogView.expenseCategory.error == null
    }
}