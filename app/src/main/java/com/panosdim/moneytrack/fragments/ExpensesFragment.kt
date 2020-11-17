package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.ExpensesAdapter
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.dialogs.ExpensesFilterDialog
import com.panosdim.moneytrack.dialogs.ExpensesSortDialog
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import kotlinx.android.synthetic.main.fragment_expenses.*
import kotlinx.android.synthetic.main.fragment_expenses.view.*

class ExpensesFragment : Fragment() {
    private lateinit var expensesView: View
    private val expensesViewAdapter =
            ExpensesAdapter(mutableListOf(), mutableListOf()) { expenseItem: Expense ->
                expenseItemClicked(expenseItem)
            }
    private val expenseDialog: ExpenseDialog = ExpenseDialog()
    private val expensesSortDialog: ExpensesSortDialog = ExpensesSortDialog()
    private val expensesFilterDialog: ExpensesFilterDialog = ExpensesFilterDialog()
    private val viewModel: ExpensesViewModel by viewModels(ownerProducer = { this })

    private fun expenseItemClicked(expItem: Expense) {
        expenseDialog.showNow(childFragmentManager, "ExpenseDialog")
        expenseDialog.showForm(expItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.expenses.observe(viewLifecycleOwner) { list ->
            rvExpenses.adapter =
                    viewModel.categories.value?.let {
                        ExpensesAdapter(list, it) { expenseItem: Expense ->
                            expenseItemClicked(expenseItem)
                        }
                    }
            rvExpenses.adapter?.let {
                (rvExpenses.adapter as ExpensesAdapter).notifyDataSetChanged()
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            rvExpenses.adapter =
                    viewModel.expenses.value?.let {
                        ExpensesAdapter(it, list) { expenseItem: Expense ->
                            expenseItemClicked(expenseItem)
                        }
                    }
            rvExpenses.adapter?.let {
                (rvExpenses.adapter as ExpensesAdapter).notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        expensesView = inflater.inflate(R.layout.fragment_expenses, container, false)

        val expensesRV = expensesView.rvExpenses
        expensesRV.setHasFixedSize(true)
        expensesRV.layoutManager = LinearLayoutManager(expensesView.context)
        expensesRV.adapter = expensesViewAdapter

        expensesView.addNewExpense.setOnClickListener {
            expenseDialog.showNow(childFragmentManager, "ExpenseDialog")
            expenseDialog.showForm(null)
        }

        expensesView.filterExpenses.setOnClickListener {
            expensesFilterDialog.showNow(childFragmentManager, "ExpensesFilterDialog")
        }

        expensesView.sortExpenses.setOnClickListener {
            expensesSortDialog.showNow(childFragmentManager, "ExpensesSortDialog")
        }

        return expensesView
    }
}