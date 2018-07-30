package com.panosdim.moneytrack

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
import com.panosdim.moneytrack.network.GetJsonData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_income.view.*
import org.json.JSONArray
import org.json.JSONObject


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
                    view.context.startActivity(intent)
                }
                1 -> Toast.makeText(this, "You select Expenses",
                        Toast.LENGTH_LONG).show()
                2 -> Toast.makeText(this, "You select Categories",
                        Toast.LENGTH_LONG).show()
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

        private val data: MutableList<Income> = mutableListOf()
        private lateinit var mView: View
        private lateinit var viewAdapter: RecyclerView.Adapter<*>

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            mView = inflater.inflate(R.layout.fragment_income, container, false)
            if (data.size == 0) {
                GetJsonData(::getIncomeDataTask).execute("php/get_income.php")
            } else {
                viewAdapter = IncomeAdapter(data) { incItem: Income -> incomeItemClicked(incItem) }

                val recyclerView = mView.rvIncome
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(mView.context)
                recyclerView.adapter = viewAdapter
            }

            return when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> mView
                2 -> inflater.inflate(R.layout.fragment_expenses, container, false)
                3 -> inflater.inflate(R.layout.fragment_categories, container, false)
                else -> inflater.inflate(R.layout.fragment_income, container, false)
            }
        }

        private fun getIncomeDataTask(result: String) {
            if (!result.isEmpty()) {
                // Convert JSON response to List<Income>
                val resp = JSONArray(result)
                for (inc in 0 until resp.length()) {
                    val item = resp.getJSONObject(inc)
                    data.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                }

                viewAdapter = IncomeAdapter(data) { incItem: Income -> incomeItemClicked(incItem) }

                val recyclerView = mView.rvIncome
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(mView.context)
                recyclerView.adapter = viewAdapter
            }
        }

        private fun incomeItemClicked(incItem: Income) {
            val intent = Intent(mView.context, IncomeDetails::class.java)
            val bundle = Bundle()
            bundle.putParcelable(INCOME_MESSAGE, incItem)
            intent.putExtras(bundle)
            mView.context.startActivity(intent)
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
