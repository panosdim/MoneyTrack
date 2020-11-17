package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import kotlinx.android.synthetic.main.dialog_income_sort.view.*


class IncomeSortDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_income_sort, container, false)

        dialogView.rgIncField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbDate -> viewModel.sortField = IncomeViewModel.SortField.DATE
                R.id.rbSalary -> viewModel.sortField = IncomeViewModel.SortField.AMOUNT
                R.id.rbComment -> viewModel.sortField = IncomeViewModel.SortField.COMMENT
            }
            viewModel.refreshIncome()
        }

        dialogView.rgIncDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbAscending -> viewModel.sortDirection = IncomeViewModel.SortDirection.ASC
                R.id.rbDescending -> viewModel.sortDirection = IncomeViewModel.SortDirection.DESC
            }
            viewModel.refreshIncome()
        }

        return dialogView
    }
}