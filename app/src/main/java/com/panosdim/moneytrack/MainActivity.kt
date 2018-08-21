package com.panosdim.moneytrack

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
import com.panosdim.moneytrack.income.FilterIncome
import com.panosdim.moneytrack.income.Income
import com.panosdim.moneytrack.income.IncomeAdapter
import com.panosdim.moneytrack.income.IncomeDetails
import com.panosdim.moneytrack.network.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.android.synthetic.main.fragment_expenses.view.*
import kotlinx.android.synthetic.main.fragment_income.view.*
import org.json.JSONArray
import org.json.JSONObject

const val INCOME_CODE = 0
const val EXPENSE_CODE = 1
const val CATEGORY_CODE = 2
const val FILTER_INCOME_CODE = 3

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
                if (it.isNotEmpty()) {
                    // Convert JSON response to List<Income>
                    val resp = JSONArray(it)
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        incomeList.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                    }
                    if (container.rvIncome != null) {
                        container.rvIncome.adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        if (categoriesList.size == 0) {
            getCategories {
                if (it.isNotEmpty()) {
                    // Convert JSON response to List<Category>
                    val resp = JSONArray(it)
                    for (inc in 0 until resp.length()) {
                        val item = resp.getJSONObject(inc)
                        categoriesList.add(Category(item.getString("id"), item.getString("category")))
                    }
                    if (container.rvCategories != null) {
                        container.rvCategories.adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        fabAdd.setOnClickListener { view ->
            when (tabs.selectedTabPosition) {
                0 -> {
                    val intent = Intent(view!!.context, IncomeDetails::class.java)
                    startActivityForResult(intent, INCOME_CODE)
                }
                1 -> {
                    val intent = Intent(view!!.context, ExpenseDetails::class.java)
                    startActivityForResult(intent, EXPENSE_CODE)
                }
                2 -> {
                    val intent = Intent(view!!.context, CategoryDetails::class.java)
                    startActivityForResult(intent, CATEGORY_CODE)
                }
            }
        }

        fabSearch.setOnClickListener { view ->
            when (tabs.selectedTabPosition) {
                0 -> {
                    val intent = Intent(view!!.context, FilterIncome::class.java)
                    startActivityForResult(intent, FILTER_INCOME_CODE)
                }
                1 -> {
                    val intent = Intent(view!!.context, ExpenseDetails::class.java)
                    startActivityForResult(intent, EXPENSE_CODE)
                }
                2 -> {
                    val intent = Intent(view!!.context, CategoryDetails::class.java)
                    startActivityForResult(intent, CATEGORY_CODE)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkForActiveSession {
            val res = JSONObject(it)
            if (!res.getBoolean("loggedIn")) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(LOGGEDOUT_MESSAGE, true)
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CATEGORY_CODE) {
                container.rvCategories.adapter.notifyDataSetChanged()
                // Fetch again expenses if we change category name
                expensesList.clear()
                getExpenses(::expenseTask)
            }

            if (requestCode == INCOME_CODE) {
                container.rvIncome.adapter.notifyDataSetChanged()
            }

            if (requestCode == EXPENSE_CODE) {
                container.rvExpenses.adapter.notifyDataSetChanged()
            }

            if (requestCode == FILTER_INCOME_CODE) {
                container.rvIncome.adapter.notifyDataSetChanged()
            }
        }
    }

    private fun expenseTask(result: String) {
        if (result.isNotEmpty()) {
            // Convert JSON response to List<Income>
            val resp = JSONArray(result)
            for (inc in 0 until resp.length()) {
                val item = resp.getJSONObject(inc)
                expensesList.add(Expense(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("category"), item.getString("comment")))
            }

            if (container.rvExpenses != null) {
                container.rvExpenses.adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            logout()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        Toast.makeText(this, "Logging you out!",
                Toast.LENGTH_LONG).show()
        logout {
            val res = JSONObject(it)
            if (res.getBoolean("loggedOut")) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(LOGGEDOUT_MESSAGE, true)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Fail to log you out!",
                        Toast.LENGTH_LONG).show()
            }
        }
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

            incomeView = inflater.inflate(R.layout.fragment_income, container, false)
            incomeViewAdapter = IncomeAdapter(incomeList) { incItem: Income -> incomeItemClicked(incItem) }

            val incomeRV = incomeView.rvIncome
            incomeRV.setHasFixedSize(true)
            incomeRV.layoutManager = LinearLayoutManager(incomeView.context)
            incomeRV.adapter = incomeViewAdapter

            expenseView = inflater.inflate(R.layout.fragment_expenses, container, false)
            expenseViewAdapter = ExpenseAdapter(expensesList) { expItem: Expense -> expenseItemClicked(expItem) }

            val expenseRV = expenseView.rvExpenses
            expenseRV.setHasFixedSize(true)
            expenseRV.layoutManager = LinearLayoutManager(expenseView.context)
            expenseRV.adapter = expenseViewAdapter

            categoryView = inflater.inflate(R.layout.fragment_categories, container, false)
            categoryViewAdapter = CategoryAdapter(categoriesList) { catItem: Category -> categoryItemClicked(catItem) }

            val categoryRV = categoryView.rvCategories
            categoryRV.setHasFixedSize(true)
            categoryRV.layoutManager = LinearLayoutManager(categoryView.context)
            categoryRV.adapter = categoryViewAdapter

            return when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> incomeView
                2 -> expenseView
                3 -> categoryView
                else -> incomeView
            }
        }

        private fun expenseItemClicked(expItem: Expense) {
            val intent = Intent(expenseView.context, ExpenseDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(EXPENSE_MESSAGE, expItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, EXPENSE_CODE)
        }

        private fun categoryItemClicked(catItem: Category) {
            val intent = Intent(categoryView.context, CategoryDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(CATEGORY_MESSAGE, catItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, CATEGORY_CODE)
        }

        private fun incomeItemClicked(incItem: Income) {
            val intent = Intent(incomeView.context, IncomeDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(INCOME_MESSAGE, incItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, INCOME_CODE)
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
