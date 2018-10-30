package com.panosdim.moneytrack

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Toast
import com.panosdim.moneytrack.category.Category
import com.panosdim.moneytrack.category.CategoryAdapter
import com.panosdim.moneytrack.category.CategoryDetails
import com.panosdim.moneytrack.expense.Expense
import com.panosdim.moneytrack.expense.ExpenseAdapter
import com.panosdim.moneytrack.expense.ExpenseDetails
import com.panosdim.moneytrack.expense.FilterExpenses
import com.panosdim.moneytrack.income.FilterIncome
import com.panosdim.moneytrack.income.Income
import com.panosdim.moneytrack.income.IncomeAdapter
import com.panosdim.moneytrack.income.IncomeDetails
import com.panosdim.moneytrack.network.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.android.synthetic.main.fragment_expenses.view.*
import kotlinx.android.synthetic.main.fragment_income.view.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class MainActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        if (expensesList.size == 0) {
            getExpenses(::expenseTask)
        }

        if (incomeList.size == 0) {
            getIncome {
                val res = JSONObject(it)
                if (res.getBoolean("success")) {
                    // Convert JSON response to List<Income>
                    val resp = res.getJSONArray("data")
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        incomeList.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                    }
                    container.rvIncome?.adapter?.notifyDataSetChanged()
                    calculateIncomeTotal()
                } else {
                    Toast.makeText(this, res.getString("message"),
                            Toast.LENGTH_LONG).show()
                }
            }
        }

        if (categoriesList.size == 0) {
            getCategories {
                val res = JSONObject(it)
                if (res.getBoolean("success")) {
                    // Convert JSON response to List<Category>
                    val resp = res.getJSONArray("data")
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        categoriesList.add(Category(item.getString("id"), item.getString("category")))
                    }
                    container.rvCategories?.adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, res.getString("message"),
                            Toast.LENGTH_LONG).show()
                }
            }
        }

        fabAdd.setOnClickListener {
            when (tabs.selectedTabPosition) {
                0 -> {
                    val intent = Intent(this, IncomeDetails::class.java)
                    startActivityForResult(intent, Operations.INCOME.code)
                }
                1 -> {
                    val intent = Intent(this, ExpenseDetails::class.java)
                    startActivityForResult(intent, Operations.EXPENSE.code)
                }
                2 -> {
                    val intent = Intent(this, CategoryDetails::class.java)
                    startActivityForResult(intent, Operations.CATEGORY.code)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val jsonParam = JSONObject()
        jsonParam.put("token", prefs.token)
        jsonParam.put("selector", prefs.selector)
        jsonParam.put("series", prefs.series)

        checkForActiveSession({
            val res = JSONObject(it)
            if (!res.getBoolean("success")) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(LOGGEDOUT_MESSAGE, true)
                startActivity(intent)
            } else {
                prefs.token = res.getString("data")
                calculateIncomeTotal()
                calculateExpensesTotal()
            }
        }, jsonParam)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Operations.CATEGORY.code) {
                container.rvCategories.adapter?.notifyDataSetChanged()
                // Fetch again expenses if we change category name
                expensesList.clear()
                getExpenses(::expenseTask)
            }

            if (requestCode == Operations.INCOME.code) {
                sortIncome()
                calculateIncomeTotal()
            }

            if (requestCode == Operations.EXPENSE.code) {
                sortExpenses()
                calculateExpensesTotal()
            }

            if (requestCode == Operations.FILTER_INCOME.code) {
                sortIncome()
                calculateIncomeTotal()
            }

            if (requestCode == Operations.FILTER_EXPENSE.code) {
                sortExpenses()
                calculateExpensesTotal()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculateIncomeTotal() {
        var total = 0.0
        for (income in incomeList) {
            total += income.salary.toDouble()
        }

        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        val moneyFormat = DecimalFormat("#,##0.00 €", symbols)

        container.incTotal?.text = "${getString(R.string.total_income)} ${moneyFormat.format(total)}"
    }

    @SuppressLint("SetTextI18n")
    private fun calculateExpensesTotal() {
        var total = 0.0
        for (expense in expensesList) {
            total += expense.amount.toDouble()
        }

        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        val moneyFormat = DecimalFormat("#,##0.00 €", symbols)

        container.expTotal?.text = "${getString(R.string.total_expenses)} ${moneyFormat.format(total)}"
    }

    private fun expenseTask(result: String) {
        val res = JSONObject(result)
        if (res.getBoolean("success")) {
            // Convert JSON response to List<Expense>
            val resp = res.getJSONArray("data")
            for (inc in 0 until resp.length()) {
                val item = resp.getJSONObject(inc)
                expensesList.add(Expense(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("category"), item.getString("comment")))
            }
            sortExpenses()
            calculateExpensesTotal()
        } else {
            Toast.makeText(this, res.getString("message"),
                    Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            Toast.makeText(this, "Logging you out!",
                    Toast.LENGTH_LONG).show()

            val jsonParam = JSONObject()
            jsonParam.put("selector", prefs.selector)

            logout ({
                val res = JSONObject(it)
                if (res.getBoolean("success")) {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra(LOGGEDOUT_MESSAGE, true)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Fail to log you out!",
                            Toast.LENGTH_LONG).show()
                }
            }, jsonParam)
            true
        }

        R.id.action_filter -> {
            when (tabs.selectedTabPosition) {
                0 -> {
                    val intent = Intent(this, FilterIncome::class.java)
                    startActivityForResult(intent, Operations.FILTER_INCOME.code)
                }
                1 -> {
                    val intent = Intent(this, FilterExpenses::class.java)
                    startActivityForResult(intent, Operations.FILTER_EXPENSE.code)
                }
            }
            true
        }

        R.id.action_sort -> {
            when (tabs.selectedTabPosition) {
                0 -> {
                    container.sortIncome.visibility = if (container.sortIncome.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
                1 -> {
                    container.sortExpenses.visibility = if (container.sortExpenses.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun sortIncome() {
        when (container.rgIncField?.checkedRadioButtonId) {
            R.id.rbDate -> {
                when (container.rgIncDirection?.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        incomeList.sortBy { it.date }
                    }
                    R.id.rbDescending -> {
                        incomeList.sortByDescending { it.date }
                    }
                }
            }

            R.id.rbSalary -> {
                when (container.rgIncDirection?.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        incomeList.sortBy { it.salary.toDouble() }
                    }
                    R.id.rbDescending -> {
                        incomeList.sortByDescending { it.salary.toDouble() }
                    }
                }
            }

            R.id.rbComment -> {
                when (container.rgIncDirection?.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        incomeList.sortBy { it.comment }
                    }
                    R.id.rbDescending -> {
                        incomeList.sortByDescending { it.comment }
                    }
                }
            }

            else -> {
            }
        }
        container.rvIncome?.adapter?.notifyDataSetChanged()
    }

    fun sortExpenses() {
        when (container.rgExpField?.checkedRadioButtonId) {
            R.id.rbExpDate -> {
                when (container.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.date }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.date }
                    }
                }
            }

            R.id.rbAmount -> {
                when (container.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.amount.toDouble() }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.amount.toDouble() }
                    }
                }
            }

            R.id.rbCategory -> {
                when (container.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.category }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.category }
                    }
                }
            }

            R.id.rbExpComment -> {
                when (container.rgExpDirection?.checkedRadioButtonId) {
                    R.id.rbExpAscending -> {
                        expensesList.sortBy { it.comment }
                    }
                    R.id.rbExpDescending -> {
                        expensesList.sortByDescending { it.comment }
                    }
                }
            }

            else -> {
            }
        }
        container.rvExpenses?.adapter?.notifyDataSetChanged()
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        private lateinit var incomeView: View
        private lateinit var incomeViewAdapter: RecyclerView.Adapter<*>

        private lateinit var expenseView: View
        private lateinit var expenseViewAdapter: RecyclerView.Adapter<*>

        private lateinit var categoryView: View
        private lateinit var categoryViewAdapter: RecyclerView.Adapter<*>

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            return when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> {
                    incomeView = inflater.inflate(R.layout.fragment_income, container, false)
                    incomeViewAdapter = IncomeAdapter(incomeList) { incItem: Income -> incomeItemClicked(incItem) }

                    val incomeRV = incomeView.rvIncome
                    incomeRV.setHasFixedSize(true)
                    incomeRV.layoutManager = LinearLayoutManager(incomeView.context)
                    incomeRV.adapter = incomeViewAdapter

                    // Sort Income By Date, Salary and Comment
                    incomeView.rgIncField.setOnCheckedChangeListener { _, _ ->
                        sortIncome()
                    }

                    incomeView.rgIncDirection.setOnCheckedChangeListener { _, _ ->
                        sortIncome()
                    }

                    calculateIncomeTotal()
                    return incomeView
                }
                2 -> {
                    expenseView = inflater.inflate(R.layout.fragment_expenses, container, false)
                    expenseViewAdapter = ExpenseAdapter(expensesList) { expItem: Expense -> expenseItemClicked(expItem) }

                    val expenseRV = expenseView.rvExpenses
                    expenseRV.setHasFixedSize(true)
                    expenseRV.layoutManager = LinearLayoutManager(expenseView.context)
                    expenseRV.adapter = expenseViewAdapter

                    // Sort Expenses By Date, Expense, Category and Comment
                    expenseView.rgExpField.setOnCheckedChangeListener { _, _ ->
                        sortExpenses()
                    }

                    expenseView.rgExpDirection.setOnCheckedChangeListener { _, _ ->
                        sortExpenses()
                    }

                    calculateExpensesTotal()
                    return expenseView
                }
                3 -> {
                    categoryView = inflater.inflate(R.layout.fragment_categories, container, false)
                    categoryViewAdapter = CategoryAdapter(categoriesList) { catItem: Category -> categoryItemClicked(catItem) }

                    val categoryRV = categoryView.rvCategories
                    categoryRV.setHasFixedSize(true)
                    categoryRV.layoutManager = LinearLayoutManager(categoryView.context)
                    categoryRV.adapter = categoryViewAdapter
                    return categoryView
                }
                else -> {
                    null
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun calculateIncomeTotal() {
            var total = 0.0
            for (income in incomeList) {
                total += income.salary.toDouble()
            }

            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0.00 €", symbols)

            incomeView.incTotal?.text = "${getString(R.string.total_income)} ${moneyFormat.format(total)}"
        }

        @SuppressLint("SetTextI18n")
        private fun calculateExpensesTotal() {
            var total = 0.0
            for (expense in expensesList) {
                total += expense.amount.toDouble()
            }

            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0.00 €", symbols)

            expenseView.expTotal?.text = "${getString(R.string.total_expenses)} ${moneyFormat.format(total)}"
        }

        private fun sortIncome() {
            when (incomeView.rgIncField?.checkedRadioButtonId) {
                R.id.rbDate -> {
                    when (incomeView.rgIncDirection?.checkedRadioButtonId) {
                        R.id.rbAscending -> {
                            incomeList.sortBy { it.date }
                        }
                        R.id.rbDescending -> {
                            incomeList.sortByDescending { it.date }
                        }
                    }
                }

                R.id.rbSalary -> {
                    when (incomeView.rgIncDirection?.checkedRadioButtonId) {
                        R.id.rbAscending -> {
                            incomeList.sortBy { it.salary.toDouble() }
                        }
                        R.id.rbDescending -> {
                            incomeList.sortByDescending { it.salary.toDouble() }
                        }
                    }
                }

                R.id.rbComment -> {
                    when (incomeView.rgIncDirection?.checkedRadioButtonId) {
                        R.id.rbAscending -> {
                            incomeList.sortBy { it.comment }
                        }
                        R.id.rbDescending -> {
                            incomeList.sortByDescending { it.comment }
                        }
                    }
                }

                else -> {
                }
            }
            incomeView.rvIncome?.adapter?.notifyDataSetChanged()
        }

        private fun sortExpenses() {
            when (expenseView.rgExpField?.checkedRadioButtonId) {
                R.id.rbExpDate -> {
                    when (expenseView.rgExpDirection?.checkedRadioButtonId) {
                        R.id.rbExpAscending -> {
                            expensesList.sortBy { it.date }
                        }
                        R.id.rbExpDescending -> {
                            expensesList.sortByDescending { it.date }
                        }
                    }
                }

                R.id.rbAmount -> {
                    when (expenseView.rgExpDirection?.checkedRadioButtonId) {
                        R.id.rbExpAscending -> {
                            expensesList.sortBy { it.amount.toDouble() }
                        }
                        R.id.rbExpDescending -> {
                            expensesList.sortByDescending { it.amount.toDouble() }
                        }
                    }
                }

                R.id.rbCategory -> {
                    when (expenseView.rgExpDirection?.checkedRadioButtonId) {
                        R.id.rbExpAscending -> {
                            expensesList.sortBy { it.category }
                        }
                        R.id.rbExpDescending -> {
                            expensesList.sortByDescending { it.category }
                        }
                    }
                }

                R.id.rbExpComment -> {
                    when (expenseView.rgExpDirection?.checkedRadioButtonId) {
                        R.id.rbExpAscending -> {
                            expensesList.sortBy { it.comment }
                        }
                        R.id.rbExpDescending -> {
                            expensesList.sortByDescending { it.comment }
                        }
                    }
                }

                else -> {
                }
            }
            expenseView.rvExpenses?.adapter?.notifyDataSetChanged()
        }

        private fun expenseItemClicked(expItem: Expense) {
            val intent = Intent(expenseView.context, ExpenseDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(EXPENSE_MESSAGE, expItem)
            intent.putExtras(bundle)
            activity?.startActivityForResult(intent, Operations.EXPENSE.code)
        }

        private fun categoryItemClicked(catItem: Category) {
            val intent = Intent(categoryView.context, CategoryDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(CATEGORY_MESSAGE, catItem)
            intent.putExtras(bundle)
            activity?.startActivityForResult(intent, Operations.CATEGORY.code)
        }

        private fun incomeItemClicked(incItem: Income) {
            val intent = Intent(incomeView.context, IncomeDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(INCOME_MESSAGE, incItem)
            intent.putExtras(bundle)
            activity?.startActivityForResult(intent, Operations.INCOME.code)
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): PlaceholderFragment {
                val fragment = PlaceholderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
    }
}
