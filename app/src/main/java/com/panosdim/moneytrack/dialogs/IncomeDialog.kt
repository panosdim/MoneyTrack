package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.*
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import kotlinx.android.synthetic.main.dialog_income.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class IncomeDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private var income: Income? = null
    private val textWatcher = generateTextWatcher(::validateForm)
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val incomeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val sqlDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var dateSelected: LocalDate = LocalDate.now()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_income, container, false)

        dialogView.incomeAmount.filters = arrayOf<InputFilter>(
                DecimalDigitsInputFilter(
                        5,
                        2
                )
        )

        dialogView.incomeComment.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                income?.let {
                    updateIncome(it)
                } ?: kotlin.run {
                    saveIncome()
                }
            }
            false
        }

        dialogView.incomeDate.setOnClickListener {
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
                dialogView.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            }

            picker.show(childFragmentManager, picker.toString())
        }

        dialogView.saveIncome.setOnClickListener {
            dialogView.prgIndicator.visibility = View.VISIBLE
            dialogView.saveIncome.isEnabled = false
            dialogView.deleteIncome.isEnabled = false

            income?.let {
                updateIncome(it)
            } ?: kotlin.run {
                saveIncome()
            }
        }

        dialogView.deleteIncome.setOnClickListener {
            deleteIncome()
        }

        return dialogView
    }

    override fun onPause() {
        super.onPause()
        dateSelected = LocalDate.now()
    }

    private fun updateIncome(income: Income) {
        // Check if we change something in the object
        if (income.date == dateSelected.format(sqlDateFormatter) &&
                income.amount == dialogView.incomeAmount.text.toString().toFloat() &&
                income.comment == dialogView.incomeComment.text.toString()
        ) {
            dismiss()
        } else {
            // Update Income
            income.date = dateSelected.format(sqlDateFormatter)
            income.amount = dialogView.incomeAmount.text.toString().toFloat()
            income.comment = dialogView.incomeComment.text.toString()

            viewModel.updateIncome(income).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteIncome.isEnabled = true
                            dialogView.saveIncome.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteIncome.isEnabled = true
                            dialogView.saveIncome.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteIncome.isEnabled = false
                            dialogView.saveIncome.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun deleteIncome() {
        income?.let {
            viewModel.removeIncome(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dismiss()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteIncome.isEnabled = true
                            dialogView.saveIncome.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteIncome.isEnabled = true
                            dialogView.saveIncome.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteIncome.isEnabled = false
                            dialogView.saveIncome.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveIncome() {
        val newIncome = Income(
                null,
                dateSelected.format(sqlDateFormatter),
                dialogView.incomeAmount.text.toString().toFloat(),
                dialogView.incomeComment.text.toString()
        )

        viewModel.addIncome(newIncome).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dismiss()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteIncome.isEnabled = true
                        dialogView.saveIncome.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                        ).show()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteIncome.isEnabled = true
                        dialogView.saveIncome.isEnabled = true
                    }
                    is Resource.Loading -> {
                        dialogView.prgIndicator.visibility = View.VISIBLE
                        dialogView.deleteIncome.isEnabled = false
                        dialogView.saveIncome.isEnabled = false
                    }
                }
            }
        }
    }

    private fun validateForm() {
        val incomeDate = dialogView.incomeDate
        val incomeAmount = dialogView.incomeAmount
        val saveIncome = dialogView.saveIncome
        saveIncome.isEnabled = true
        incomeDate.error = null
        incomeAmount.error = null

        // Store values.
        val date = incomeDate.text.toString()
        val salary = incomeAmount.text.toString()

        // Check for a valid date.
        if (date.isEmpty()) {
            incomeDate.error = getString(R.string.error_field_required)
            saveIncome.isEnabled = false
        }

        // Check for a valid salary.
        if (salary.isEmpty()) {
            incomeAmount.error = getString(R.string.error_field_required)
            saveIncome.isEnabled = false
        }
    }

    fun showForm(incItem: Income?) {
        dialogView.prgIndicator.visibility = View.GONE
        dialogView.saveIncome.isEnabled = true
        dialogView.deleteIncome.isEnabled = true

        dialogView.incomeDate.removeTextChangedListener(textWatcher)
        dialogView.incomeDate.error = null

        dialogView.incomeAmount.removeTextChangedListener(textWatcher)
        dialogView.incomeAmount.error = null

        dialogView.incomeComment.removeTextChangedListener(textWatcher)
        dialogView.incomeComment.error = null

        income = incItem
        if (incItem == null) {
            dialogView.incomeDate.addTextChangedListener(textWatcher)
            dialogView.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            dialogView.incomeAmount.addTextChangedListener(textWatcher)
            dialogView.incomeAmount.setText("")
            dialogView.incomeComment.addTextChangedListener(textWatcher)
            dialogView.incomeComment.setText("")
            dialogView.deleteIncome.visibility = View.GONE
            dialogView.saveIncome.setText(R.string.save)
        } else {
            dateSelected = try {
                LocalDate.parse(incItem.date)
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }
            dialogView.incomeDate.setText(dateSelected.toShowDateFormat(incomeDateFormatter))
            dialogView.incomeAmount.setText(incItem.amount.toString())
            dialogView.incomeComment.setText(incItem.comment)
            dialogView.deleteIncome.visibility = View.VISIBLE
            dialogView.saveIncome.setText(R.string.update)
            dialogView.incomeDate.addTextChangedListener(textWatcher)
            dialogView.incomeAmount.addTextChangedListener(textWatcher)
            dialogView.incomeComment.addTextChangedListener(textWatcher)
        }
    }

    private fun isFormValid(): Boolean {
        return dialogView.incomeDate.error == null && dialogView.incomeAmount.error == null
    }
}