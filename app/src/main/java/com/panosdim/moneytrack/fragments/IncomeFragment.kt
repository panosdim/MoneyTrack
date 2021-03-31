package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.IncomeAdapter
import com.panosdim.moneytrack.dialogs.IncomeDialog
import com.panosdim.moneytrack.dialogs.IncomeFilterDialog
import com.panosdim.moneytrack.dialogs.IncomeSortDialog
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import kotlinx.android.synthetic.main.fragment_income.*
import kotlinx.android.synthetic.main.fragment_income.view.*

class IncomeFragment : Fragment() {
    private lateinit var incomeView: View
    private val incomeViewAdapter =
        IncomeAdapter(mutableListOf()) { incomeItem: Income ->
            incomeItemClicked(
                incomeItem
            )
        }
    private val incomeDialog: IncomeDialog = IncomeDialog()
    private val incomeSortDialog: IncomeSortDialog = IncomeSortDialog()
    private val incomeFilterDialog: IncomeFilterDialog = IncomeFilterDialog()
    private val viewModel: IncomeViewModel by viewModels(ownerProducer = { this })

    private fun incomeItemClicked(incItem: Income) {
        incomeDialog.showNow(childFragmentManager, "IncomeDialog")
        incomeDialog.showForm(incItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.income.observe(viewLifecycleOwner) { list ->
            rvIncome.adapter =
                IncomeAdapter(list) { incomeItem: Income ->
                    incomeItemClicked(
                        incomeItem
                    )
                }
            (rvIncome.adapter as IncomeAdapter).notifyDataSetChanged()
        }

        incSwipeRefresh.setOnRefreshListener {
            viewModel.refreshIncome()
            incSwipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        incomeView = inflater.inflate(R.layout.fragment_income, container, false)

        val incomeRV = incomeView.rvIncome
        incomeRV.setHasFixedSize(true)
        incomeRV.layoutManager = LinearLayoutManager(incomeView.context)
        incomeRV.adapter = incomeViewAdapter

        incomeView.addNewIncome.setOnClickListener {
            incomeDialog.showNow(childFragmentManager, "IncomeDialog")
            incomeDialog.showForm(null)
        }

        incomeView.filterIncome.setOnClickListener {
            incomeFilterDialog.showNow(childFragmentManager, "IncomeFilterDialog")
        }

        incomeView.sortIncome.setOnClickListener {
            incomeSortDialog.showNow(childFragmentManager, "IncomeSortDialog")
        }

        return incomeView
    }
}