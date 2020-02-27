package com.panosdim.moneytrack.activities

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.utils.downloadData
import com.panosdim.moneytrack.utils.loginWithStoredCredentials
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class DashboardActivity : AppCompatActivity(), OnChartValueSelectedListener {
    private lateinit var monthExpensesPerCategories: List<PieEntry>
    private var moneyFormat: DecimalFormat
    private val today = LocalDate.now()
    private val startOfMonth = today.withDayOfMonth(1)
    private val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
    private val startOfYear = today.withDayOfMonth(1).withMonth(1)
    private val endOfYear = today.withMonth(12).withDayOfMonth(31)

    init {
        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        moneyFormat = DecimalFormat("#,##0.00 €", symbols)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.dashboard)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private suspend fun downloadAllData() {
        layoutDashboard.visibility = View.GONE
        prgDownloadData.visibility = View.VISIBLE
        downloadData(this@DashboardActivity)
        layoutDashboard.visibility = View.VISIBLE
        prgDownloadData.visibility = View.GONE
        initializeSavings()
        initializeChart()
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                downloadAllData()
            } catch (e: HttpException) {
                loginWithStoredCredentials(this@DashboardActivity, ::downloadAllData)
            } catch (t: SocketTimeoutException) {
                Toast.makeText(this@DashboardActivity, "Connection timeout", Toast.LENGTH_LONG)
                    .show()
                finish()
            } catch (d: UnknownHostException) {
                Toast.makeText(
                    this@DashboardActivity,
                    "Unable to resolve host",
                    Toast.LENGTH_LONG
                )
                    .show()
                finish()
            }
        }
    }

    private fun initializeSavings() {
        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

        txtMonthSavingsTitle.text = monthFormatter.format(today)
        txtYearSavingsTitle.text = getString(R.string.savings_title, yearFormatter.format(today))

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

        txtMonthSavings.text = moneyFormat.format(totalMonthSavings)
        txtMonthSavings.setTextColor(
            ContextCompat.getColor(
                this,
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

        txtYearSavings.text = moneyFormat.format(totalYearSavings)
        txtYearSavings.setTextColor(
            ContextCompat.getColor(
                this,
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
        chart.invalidate()
    }

    override fun onNothingSelected() {
        chart.centerText = SpannableString("")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null && h != null) {
            val catEntry = monthExpensesPerCategories[h.x.toInt()]
            val centerString = SpannableString(catEntry.label + "\n" + moneyFormat.format(e.y))
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
                        this,
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
