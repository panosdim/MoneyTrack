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
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.adapters.ExpensesAdapter
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.model.*
import com.panosdim.moneytrack.model.ExpensesFilters.filterExpenses
import com.panosdim.moneytrack.model.ExpensesFilters.isFiltersSet
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

        expensesView.rgExpField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbExpDate -> ExpensesSort.field = SortField.DATE
                R.id.rbAmount -> ExpensesSort.field = SortField.AMOUNT
                R.id.rbCategory -> ExpensesSort.field = SortField.CATEGORY
                R.id.rbExpComment -> ExpensesSort.field = SortField.COMMENT
            }
            ExpensesSort.sort()
            expenseViewAdapter.notifyDataSetChanged()
        }

        expensesView.rgExpDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbExpAscending -> ExpensesSort.direction = SortDirection.ASC
                R.id.rbExpDescending -> ExpensesSort.direction = SortDirection.DESC
            }
            ExpensesSort.sort()
            expenseViewAdapter.notifyDataSetChanged()
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
        ExpensesSort.sort()
        if (::expenseViewAdapter.isInitialized) {
            expenseViewAdapter.notifyDataSetChanged()
        }
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

    override fun refreshView() {
        if (isFiltersSet) {
            filterExpenses()
        }
        ExpensesSort.sort()
        if (::expenseViewAdapter.isInitialized) {
            expenseViewAdapter.notifyDataSetChanged()
        }
    }
}
