package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.viewmodel.ExpensesViewModel
import com.panosdim.moneytrack.viewmodel.IncomeViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import java.time.LocalDate
import java.time.Month

class DashboardFragment : Fragment(), OnChartValueSelectedListener {
    private val expensesViewModel: ExpensesViewModel by viewModels(ownerProducer = { this })
    private val incomeViewModel: IncomeViewModel by viewModels(ownerProducer = { this })
    private lateinit var monthExpensesPerCategories: List<PieEntry>
    private val today = LocalDate.now()
    private var selectedMonth = today.month
    private var selectedYear = today.year
    private var startOfMonth = today.withDayOfMonth(1)
    private var endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
    private var startOfYear = today.withDayOfMonth(1).withMonth(1)
    private var endOfYear = today.withMonth(12).withDayOfMonth(31)
    private var incomeList: List<Income> = mutableListOf()
    private var expensesList: List<Expense> = mutableListOf()
    private var categoriesList: List<Category> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        expensesViewModel.expenses.observe(viewLifecycleOwner) {
            expensesList = it
            initializeSavings()
            initializeChart()
        }

        expensesViewModel.categories.observe(viewLifecycleOwner) {
            categoriesList = it
            initializeSavings()
            initializeChart()
        }

        incomeViewModel.income.observe(viewLifecycleOwner) {
            incomeList = it
            initializeSavings()
            initializeChart()
        }

        val monthAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            Month.values()
        )
        root.overviewMonth.setAdapter(monthAdapter)
        // Workaround as there is a bug in AutoCompleteView filter setting
        root.overviewMonth.setText(selectedMonth.name, false)

        root.overviewMonth.setOnItemClickListener { parent, _, position, _ ->
            selectedMonth = parent.getItemAtPosition(position) as Month
            val s = LocalDate.of(selectedYear, selectedMonth, 1)
            startOfMonth = s.withDayOfMonth(1)
            endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
            startOfYear = s.withDayOfMonth(1).withMonth(1)
            endOfYear = s.withMonth(12).withDayOfMonth(31)
            initializeSavings()
            initializeChart()
        }

        val currentYear = today.year
        val years = IntArray(4) { currentYear - it }
        val yearAdapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item,
            years.toTypedArray()
        )
        root.overviewYear.setAdapter(yearAdapter)
        // Workaround as there is a bug in AutoCompleteView filter setting
        root.overviewYear.setText(selectedYear.toString(), false)

        root.overviewYear.setOnItemClickListener { parent, _, position, _ ->
            selectedYear = parent.getItemAtPosition(position) as Int
            val s = LocalDate.of(selectedYear, selectedMonth, 1)
            startOfYear = s.withDayOfMonth(1).withMonth(1)
            endOfYear = s.withMonth(12).withDayOfMonth(31)
            startOfMonth = s.withDayOfMonth(1)
            endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
            initializeSavings()
            initializeChart()
        }

        return root
    }

    private fun initializeSavings() {
        val totalMonthIncome = incomeList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }
            .map { it.amount }
            .sum()

        val totalMonthExpenses = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }
            .map { it.amount }
            .sum()

        val totalMonthSavings = totalMonthIncome - totalMonthExpenses

        txtMonthSavings.text = moneyFormat(totalMonthSavings)
        txtMonthSavings.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                calculateColor(totalMonthSavings)
            )
        )

        val totalYearIncome = incomeList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }
            .map { it.amount }
            .sum()

        val totalYearExpenses = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }
            .map { it.amount }
            .sum()

        val totalYearSavings = totalYearIncome - totalYearExpenses

        txtYearSavings.text = moneyFormat(totalYearSavings)
        txtYearSavings.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                calculateColor(totalYearSavings)
            )
        )
    }

    private fun calculateColor(value: Float): Int {
        return if (value < 0) android.R.color.holo_red_dark else android.R.color.holo_green_light
    }

    private fun initializeChart() {
        monthExpensesPerCategories = expensesList.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }
            .groupBy { it.category }
            .map { (k, v) ->
                PieEntry(
                    v.map { it.amount }.sum(),
                    categoriesList.find { it.id == k }!!.category
                )

            }
            .sortedBy { it.value }

        val set = PieDataSet(monthExpensesPerCategories, "Expenses Per Categories")

        val colors: ArrayList<Int> = ArrayList()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())

        set.colors = colors

        val data = PieData(set)
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setDrawEntryLabels(false)
        chart.setOnChartValueSelectedListener(this)
        data.setValueFormatter(PercentFormatter(chart))
        chart.data = data
        chart.onTouchListener.setLastHighlighted(null)
        chart.highlightValues(null)
        chart.centerText = ""
        chart.invalidate()
    }

    override fun onNothingSelected() {
        chart.centerText = SpannableString("")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null && h != null) {
            val catEntry = monthExpensesPerCategories[h.x.toInt()]
            val centerString = SpannableString(catEntry.label + "\n" + moneyFormat(e.y))
            centerString.setSpan(RelativeSizeSpan(2f), 0, catEntry.label.length, 0)
            centerString.setSpan(
                RelativeSizeSpan(3f),
                catEntry.label.length + 1,
                centerString.length,
                0
            )
            centerString.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        android.R.color.holo_red_dark
                    )
                ),
                catEntry.label.length + 1,
                centerString.length,
                0
            )
            chart.centerText = centerString
        }
    }
}