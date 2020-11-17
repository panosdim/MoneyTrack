package com.panosdim.moneytrack.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import kotlinx.android.synthetic.main.dialog_expenses_sort.view.*

class ExpensesSortDialog : BottomSheetDialogFragment() {
    private lateinit var dialogView: View
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dialogView = inflater.inflate(R.layout.dialog_expenses_sort, container, false)

        dialogView.rgExpField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbExpDate -> viewModel.sortField = ExpensesViewModel.SortField.DATE
                R.id.rbAmount -> viewModel.sortField = ExpensesViewModel.SortField.AMOUNT
                R.id.rbCategory -> viewModel.sortField = ExpensesViewModel.SortField.CATEGORY
                R.id.rbExpComment -> viewModel.sortField = ExpensesViewModel.SortField.COMMENT
            }
            viewModel.refreshExpenses()
        }

        dialogView.rgExpDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbExpAscending -> viewModel.sortDirection = ExpensesViewModel.SortDirection.ASC
                R.id.rbExpDescending -> viewModel.sortDirection = ExpensesViewModel.SortDirection.DESC
            }
            viewModel.refreshExpenses()
        }

        return dialogView
    }
}