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
import com.panosdim.moneytrack.adapters.IncomeAdapter
import com.panosdim.moneytrack.dialogs.IncomeDialog
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.model.IncomeFilters.filterIncome
import com.panosdim.moneytrack.model.IncomeFilters.isFiltersSet
import com.panosdim.moneytrack.model.IncomeSort
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.utils.loginWithStoredCredentials
import kotlinx.android.synthetic.main.fragment_income.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class IncomeFragment : Fragment(), RefreshView {
    private lateinit var incomeView: View
    private lateinit var incomeViewAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        incomeView = inflater.inflate(R.layout.fragment_income, container, false)
        incomeViewAdapter =
            IncomeAdapter(incomeList) { incItem: Income -> incomeItemClicked(incItem) }

        val incomeRV = incomeView.rvIncome
        incomeRV.setHasFixedSize(true)
        incomeRV.layoutManager = LinearLayoutManager(incomeView.context)
        incomeRV.addItemDecoration(
            DividerItemDecoration(
                incomeRV.context,
                DividerItemDecoration.VERTICAL
            )
        )

        incomeView.rgIncField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbDate -> IncomeSort.field = SortField.DATE
                R.id.rbSalary -> IncomeSort.field = SortField.AMOUNT
                R.id.rbComment -> IncomeSort.field = SortField.COMMENT
            }
            IncomeSort.sort()
            incomeViewAdapter.notifyDataSetChanged()
        }

        incomeView.rgIncDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbAscending -> IncomeSort.direction = SortDirection.ASC
                R.id.rbDescending -> IncomeSort.direction = SortDirection.DESC
            }
            IncomeSort.sort()
            incomeViewAdapter.notifyDataSetChanged()
        }

        incomeRV.adapter = incomeViewAdapter

        return incomeView
    }

    private fun incomeItemClicked(incItem: Income) {
        IncomeDialog(requireContext(), this, incItem).show()
    }

    private suspend fun downloadIncome() {
        val response = repository.getAllIncome()
        incomeList.clear()
        incomeList.addAll(response.data)
        if (isFiltersSet) {
            filterIncome()
        }
        IncomeSort.sort()
        if (::incomeView.isInitialized) {
            incomeViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                downloadIncome()
            } catch (e: HttpException) {
                loginWithStoredCredentials(requireContext(), ::downloadIncome)
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
            filterIncome()
        }
        IncomeSort.sort()
        if (::incomeView.isInitialized) {
            incomeViewAdapter.notifyDataSetChanged()
        }
    }
}
