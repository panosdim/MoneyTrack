package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.utils.fromEpochMilli
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toShowDateFormat
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import kotlinx.android.synthetic.main.dialog_expenses_filter.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesFilterDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private var rangeDateSelected: androidx.core.util.Pair<Long, Long>? = null
    private val rangeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_expenses_filter, container, false)

        dialogView.expensesFilterAmount.setLabelFormatter { value: Float ->
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0 €", symbols)
            moneyFormat.format(value)
        }

        dialogView.setExpensesFilters.setOnClickListener {
            viewModel.filterAmount = dialogView.expensesFilterAmount.values
            viewModel.filterComment = dialogView.expensesFilterComment.text.toString()
            rangeDateSelected?.let {
                val startDate = fromEpochMilli(it.first!!)
                val endDate = fromEpochMilli(it.second!!)
                viewModel.filterDate = Pair(startDate, endDate)
            }

            if (dialogView.expensesFilterCategory.checkedChipIds.isNotEmpty()) {
                viewModel.filterCategory = dialogView.expensesFilterCategory.checkedChipIds
            } else {
                viewModel.filterCategory = null
            }
            
            viewModel.refreshExpenses()
            dismiss()
        }

        dialogView.expensesFilterDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(LocalDate.now().toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            rangeDateSelected?.let {
                builder.setSelection(rangeDateSelected)
            }
            builder.setTitleText("Select Range")

            val picker: MaterialDatePicker<androidx.core.util.Pair<Long, Long>> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                rangeDateSelected = selection
                rangeDateSelected?.let {
                    val startDate = fromEpochMilli(it.first!!).toShowDateFormat(rangeDateFormatter)
                    val endDate = fromEpochMilli(it.second!!).toShowDateFormat(rangeDateFormatter)
                    dialogView.expensesFilterDate.setText(requireContext().getString(R.string.date_filter, startDate, endDate))
                }
            }

            picker.show(childFragmentManager, picker.toString())
        }

        viewModel.expenses.value?.let { list ->
            val min = list.minByOrNull { it.amount }
            val max = list.maxByOrNull { it.amount }
            if (min != null && max != null) {
                dialogView.expensesFilterAmount.values = listOf(min.amount, max.amount)
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            list.sortedByDescending { it.count }.forEach { category ->
                val chip = layoutInflater.inflate(R.layout.row_chip_view, requireView().parent.parent as ViewGroup, false) as Chip
                chip.text = category.category
                chip.id = category.id!!
                viewModel.filterCategory?.let {
                    chip.isChecked = it.contains(chip.id)
                }
                dialogView.expensesFilterCategory.addView(chip)
            }
        }

        dialogView.clearExpensesFilters.setOnClickListener {
            viewModel.clearFilters()
            viewModel.expenses.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                dialogView.expensesFilterAmount.values = listOf(min?.amount, max?.amount)
            }
            dialogView.expensesFilterComment.setText("")
            dialogView.expensesFilterDate.setText("")
            rangeDateSelected = null

            dismiss()
        }

        return dialogView
    }

    override fun onResume() {
        super.onResume()

        viewModel.filterAmount?.let {
            dialogView.expensesFilterAmount.values = it
        } ?: kotlin.run {
            viewModel.expenses.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                dialogView.expensesFilterAmount.values = listOf(min?.amount, max?.amount)
            }
        }

        viewModel.filterDate?.let {
            val startDate = it.first.toShowDateFormat(rangeDateFormatter)
            val endDate = it.second.toShowDateFormat(rangeDateFormatter)
            dialogView.expensesFilterDate.setText(requireContext().getString(R.string.date_filter, startDate, endDate))
        } ?: kotlin.run {
            rangeDateSelected = null
            dialogView.expensesFilterDate.setText("")
        }

    }
}