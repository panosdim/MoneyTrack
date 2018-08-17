package com.panosdim.moneytrack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.*
import android.widget.Toast
import com.panosdim.moneytrack.network.GetJsonData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.android.synthetic.main.fragment_expenses.view.*
import kotlinx.android.synthetic.main.fragment_income.view.*
import org.json.JSONArray
import org.json.JSONObject


const val NEW_INCOME = 0
const val EDIT_INCOME = 1
const val NEW_EXPENSE = 3
const val EDIT_EXPENSE = 4
const val CATEGORY_CODE = 6

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

        fab.setOnClickListener { view ->
            when (tabs.selectedTabPosition) {
                0 -> {
                    val intent = Intent(view!!.context, IncomeDetails::class.java)
                    startActivityForResult(intent, NEW_INCOME)
                }
                1 -> {
                    val intent = Intent(view!!.context, ExpenseDetails::class.java)
                    startActivityForResult(intent, NEW_EXPENSE)
                }
                2 -> {
                    val intent = Intent(view!!.context, CategoryDetails::class.java)
                    startActivityForResult(intent, CATEGORY_CODE)
                }
            }

        }

        if (categories.size == 0) {
            GetJsonData(::categoriesTask).execute("php/get_categories.php")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            val bundle = data.extras
            if (bundle != null) {
                val fragment = mSectionsPagerAdapter!!.getRegisteredFragment(container.currentItem) as PlaceholderFragment
                if (requestCode == NEW_INCOME) {
                    val income = bundle.getParcelable<Parcelable>(EDIT_INCOME_MESSAGE) as Income
                    fragment.incomeData.add(income)
                    fragment.incomeData.sortByDescending { it.date }
                    container.rvIncome.adapter.notifyDataSetChanged()
                }

                if (requestCode == EDIT_INCOME) {
                    val income = bundle.getParcelable<Parcelable>(EDIT_INCOME_MESSAGE) as Income
                    if (bundle.getBoolean(DELETE_TASK)) {
                        fragment.incomeData.remove(income)
                    } else {
                        val index = fragment.incomeData.indexOfFirst { it.id == income.id }
                        fragment.incomeData[index] = income
                        fragment.incomeData.sortByDescending { it.date }
                    }
                    container.rvIncome.adapter.notifyDataSetChanged()
                }

                if (requestCode == NEW_EXPENSE) {
                    val expense = bundle.getParcelable<Parcelable>(EDIT_EXPENSE_MESSAGE) as Expense
                    fragment.expenseData.add(expense)
                    fragment.expenseData.sortByDescending { it.date }
                    container.rvExpenses.adapter.notifyDataSetChanged()
                }

                if (requestCode == EDIT_EXPENSE) {
                    val expense = bundle.getParcelable<Parcelable>(EDIT_EXPENSE_MESSAGE) as Expense
                    if (bundle.getBoolean(DELETE_TASK)) {
                        fragment.expenseData.remove(expense)
                    } else {
                        val index = fragment.expenseData.indexOfFirst { it.id == expense.id }
                        fragment.expenseData[index] = expense
                        fragment.expenseData.sortByDescending { it.date }
                    }
                    container.rvExpenses.adapter.notifyDataSetChanged()
                }
            }
            if (requestCode == CATEGORY_CODE ) {
                container.rvCategories.adapter.notifyDataSetChanged()
            }
        }
    }

    private fun categoriesTask(result: String) {
        if (!result.isEmpty()) {
            // Convert JSON response to List<Category>
            val resp = JSONArray(result)
            for (inc in 0 until resp.length()) {
                val item = resp.getJSONObject(inc)
                categories.add(Category(item.getString("id"), item.getString("category")))
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
        GetJsonData(::logoutTask).execute("php/logout.php")
    }

    private fun logoutTask(result: String) {
        val res = JSONObject(result)
        if (res.getBoolean("loggedout")) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(LOGGEDOUT_MESSAGE, true)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Fail to log you out!",
                    Toast.LENGTH_LONG).show()
        }
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private var registeredFragments = SparseArray<Fragment>()

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1)
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            registeredFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any) {
            registeredFragments.remove(position)
            super.destroyItem(container, position, `object`)
        }

        fun getRegisteredFragment(position: Int): Fragment {
            return registeredFragments.get(position)
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        val incomeData: MutableList<Income> = mutableListOf()
        private lateinit var incomeView: View
        private lateinit var incomeViewAdapter: RecyclerView.Adapter<*>

        val expenseData: MutableList<Expense> = mutableListOf()
        private lateinit var expenseView: View
        private lateinit var expenseViewAdapter: RecyclerView.Adapter<*>

        private lateinit var categoryView: View
        private lateinit var categoryViewAdapter: RecyclerView.Adapter<*>

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            incomeView = inflater.inflate(R.layout.fragment_income, container, false)
            if (incomeData.size == 0) {
                GetJsonData(::getIncomeDataTask).execute("php/get_income.php")
            } else {
                incomeViewAdapter = IncomeAdapter(incomeData) { incItem: Income -> incomeItemClicked(incItem) }

                val recyclerView = incomeView.rvIncome
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(incomeView.context)
                recyclerView.adapter = incomeViewAdapter
            }

            expenseView = inflater.inflate(R.layout.fragment_expenses, container, false)
            if (expenseData.size == 0) {
                GetJsonData(::getExpenseDataTask).execute("php/get_expense.php")
            } else {
                expenseViewAdapter = ExpenseAdapter(expenseData) { expItem: Expense -> expenseItemClicked(expItem) }

                val recyclerView = expenseView.rvExpenses
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(expenseView.context)
                recyclerView.adapter = expenseViewAdapter
            }

            categoryView = inflater.inflate(R.layout.fragment_categories, container, false)
            categoryViewAdapter = CategoryAdapter(categories) { catItem: Category -> categoryItemClicked(catItem) }

            val recyclerView = categoryView.rvCategories
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(categoryView.context)
            recyclerView.adapter = categoryViewAdapter

            return when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> incomeView
                2 -> expenseView
                3 -> categoryView
                else -> incomeView
            }
        }

        private fun getExpenseDataTask(result: String) {
            if (!result.isEmpty()) {
                // Convert JSON response to List<Income>
                val resp = JSONArray(result)
                for (inc in 0 until resp.length()) {
                    val item = resp.getJSONObject(inc)
                    expenseData.add(Expense(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("category"), item.getString("comment")))
                }

                expenseViewAdapter = ExpenseAdapter(expenseData) { expItem: Expense -> expenseItemClicked(expItem) }

                val recyclerView = expenseView.rvExpenses
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(expenseView.context)
                recyclerView.adapter = expenseViewAdapter
            }
        }

        private fun expenseItemClicked(expItem: Expense) {
            val intent = Intent(expenseView.context, ExpenseDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(EXPENSE_MESSAGE, expItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, EDIT_EXPENSE)
        }

        private fun categoryItemClicked(catItem: Category) {
            val intent = Intent(categoryView.context, CategoryDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(CATEGORY_MESSAGE, catItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, CATEGORY_CODE)
        }

        private fun getIncomeDataTask(result: String) {
            if (!result.isEmpty()) {
                // Convert JSON response to List<Income>
                val resp = JSONArray(result)
                for (inc in 0 until resp.length()) {
                    val item = resp.getJSONObject(inc)
                    incomeData.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                }

                incomeViewAdapter = IncomeAdapter(incomeData) { incItem: Income -> incomeItemClicked(incItem) }

                val recyclerView = incomeView.rvIncome
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(incomeView.context)
                recyclerView.adapter = incomeViewAdapter
            }
        }

        private fun incomeItemClicked(incItem: Income) {
            val intent = Intent(incomeView.context, IncomeDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(INCOME_MESSAGE, incItem)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, EDIT_INCOME)
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
