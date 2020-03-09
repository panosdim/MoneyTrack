package com.panosdim.moneytrack.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.ExpensesAdapter
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.ExpensesFilters.filterExpenses
import com.panosdim.moneytrack.model.ExpensesFilters.isFiltersSet
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import com.panosdim.moneytrack.utils.loginWithStoredCredentials
import kotlinx.android.synthetic.main.fragment_expenses.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

    private suspend fun downloadExpensesAndCategories() {
        val resp = repository.getAllCategories()
        categoriesList.clear()
        categoriesList.addAll(resp.data)
        categoriesList.sortByDescending { it.count }

        val response = repository.getAllExpenses()
        expensesList.clear()
        expensesList.addAll(response.data)
        if (isFiltersSet) {
            filterExpenses()
        }
        sortExpenses()
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                downloadExpensesAndCategories()
            } catch (e: HttpException) {
                loginWithStoredCredentials(requireContext(), ::downloadExpensesAndCategories)
            } catch (t: SocketTimeoutException) {
                Toast.makeText(requireContext(), "Connection timeout", Toast.LENGTH_LONG)
                    .show()
            } catch (d: UnknownHostException) {
                Toast.makeText(
                    requireContext(),
                    "Unable to resolve host",
                    Toast.LENGTH_LONG
                )
                    .show()
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
        if (::expensesView.isInitialized) {
            sortExpenses()
        }
    }
}
