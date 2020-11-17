package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.utils.fromEpochMilli
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toShowDateFormat
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import kotlinx.android.synthetic.main.dialog_income_filter.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.util.Pair as APair


class IncomeFilterDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private var rangeDateSelected: APair<Long, Long>? = null
    private val rangeDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_income_filter, container, false)

        dialogView.incomeFilterAmount.setLabelFormatter { value: Float ->
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0 €", symbols)
            moneyFormat.format(value)
        }

        dialogView.setIncomeFilters.setOnClickListener {
            viewModel.filterAmount = dialogView.incomeFilterAmount.values
            viewModel.filterComment = dialogView.incomeFilterComment.text.toString()
            rangeDateSelected?.let {
                val startDate = fromEpochMilli(it.first!!)
                val endDate = fromEpochMilli(it.second!!)
                viewModel.filterDate = Pair(startDate, endDate)
            }

            viewModel.refreshIncome()
            dismiss()
        }

        dialogView.incomeFilterDate.setOnClickListener {
            //Date Picker
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val constraintsBuilder = CalendarConstraints.Builder()
            constraintsBuilder.setOpenAt(LocalDate.now().toEpochMilli())
            builder.setCalendarConstraints(constraintsBuilder.build())
            rangeDateSelected?.let {
                builder.setSelection(rangeDateSelected)
            }
            builder.setTitleText("Select Range")

            val picker: MaterialDatePicker<APair<Long, Long>> = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                rangeDateSelected = selection
                rangeDateSelected?.let {
                    val startDate = fromEpochMilli(it.first!!).toShowDateFormat(rangeDateFormatter)
                    val endDate = fromEpochMilli(it.second!!).toShowDateFormat(rangeDateFormatter)
                    dialogView.incomeFilterDate.setText(requireContext().getString(R.string.date_filter, startDate, endDate))
                }
            }

            picker.show(childFragmentManager, picker.toString())
        }

        viewModel.income.value?.let { list ->
            val min = list.minByOrNull { it.amount }
            val max = list.maxByOrNull { it.amount }
            if (min != null && max != null) {
                dialogView.incomeFilterAmount.values = listOf(min.amount, max.amount)
            }
        }

        dialogView.clearIncomeFilters.setOnClickListener {
            viewModel.clearFilters()
            viewModel.income.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                dialogView.incomeFilterAmount.values = listOf(min?.amount, max?.amount)
            }
            dialogView.incomeFilterComment.setText("")
            dialogView.incomeFilterDate.setText("")
            rangeDateSelected = null

            dismiss()
        }

        return dialogView
    }

    override fun onResume() {
        super.onResume()

        viewModel.filterAmount?.let {
            dialogView.incomeFilterAmount.values = it
        } ?: kotlin.run {
            viewModel.income.value?.let { list ->
                val min = list.minByOrNull { it.amount }
                val max = list.maxByOrNull { it.amount }
                dialogView.incomeFilterAmount.values = listOf(min?.amount, max?.amount)
            }
        }

        viewModel.filterDate?.let {
            val startDate = it.first.toShowDateFormat(rangeDateFormatter)
            val endDate = it.second.toShowDateFormat(rangeDateFormatter)
            dialogView.incomeFilterDate.setText(requireContext().getString(R.string.date_filter, startDate, endDate))
        } ?: kotlin.run {
            rangeDateSelected = null
            dialogView.incomeFilterDate.setText("")
        }

    }
}