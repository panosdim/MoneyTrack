package com.panosdim.moneytrack.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.LoginActivity
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.ExpensesAdapter
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.ExpensesFilters.filterExpenses
import com.panosdim.moneytrack.model.ExpensesFilters.isFiltersSet
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import kotlinx.android.synthetic.main.fragment_expenses.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ExpensesFragment : Fragment(), RefreshView {

    private lateinit var expensesView: View
    private lateinit var expenseViewAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        expensesView = inflater.inflate(R.layout.fragment_expenses, container, false)
        expenseViewAdapter =
            ExpensesAdapter(expensesList) { expItem: Expense -> expenseItemClicked(expItem) }

        val expensesRV = expensesView.rvExpenses
        expensesRV.setHasFixedSize(true)
        expensesRV.layoutManager = LinearLayoutManager(expensesView.context)
        expensesRV.addItemDecoration(
            DividerItemDecoration(
                expensesRV.context,
                DividerItemDecoration.VERTICAL
            )
        )

        expensesView.rgExpField.setOnCheckedChangeListener { _, _ ->
            sortExpenses()
        }

        expensesView.rgExpDirection.setOnCheckedChangeListener { _, _ ->
            sortExpenses()
        }

        expensesRV.adapter = expenseViewAdapter

        return expensesView
    }

    private fun expenseItemClicked(expItem: Expense) {
        ExpenseDialog(requireContext(), this, expItem).show()
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val response = repository.getAllExpenses()
                expensesList.clear()
                expensesList.addAll(response.data)
                if (isFiltersSet) {
                    filterExpenses()
                }
                sortExpenses()
            } catch (e: HttpException) {
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun sortExpenses() {
        when (expensesView.rgExpField?.checkedRadioButtonId) {
            R.id.rbExpDate -> {
                when (expensesView.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.date }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.date }
                    }
                }
            }

            R.id.rbAmount -> {
                when (expensesView.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.amount.toDouble() }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.amount.toDouble() }
                    }
                }
            }

            R.id.rbCategory -> {
                when (expensesView.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.category }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.category }
                    }
                }
            }

            R.id.rbExpComment -> {
                when (expensesView.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.comment }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.comment }
                    }
                }
            }
        }
        expensesView.rvExpenses?.adapter?.notifyDataSetChanged()
    }

    override fun refreshView() {
        if (isFiltersSet) {
            filterExpenses()
        }
        sortExpenses()
    }
}
