package com.panosdim.moneytrack

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.LayoutManager
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.lang.ref.WeakReference


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
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        LogoutTask(this).execute()
    }

    companion object {
        class LogoutTask internal constructor(context: Context) : AsyncTask<Void, Void, String>() {
            private val context: WeakReference<Context> = WeakReference(context)

            override fun doInBackground(vararg params: Void): String? {
                val wsh = WebServiceHandler()
                return wsh.performGetCall("php/logout.php")
            }

            override fun onPostExecute(success: String?) {
                val intent = Intent(context.get(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(EXTRA_MESSAGE, true)
                context.get()!!.startActivity(intent)
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
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {

            val incomeView = inflater.inflate(R.layout.fragment_income, container, false)
            GetIncomeDataTask(incomeView).execute()

            when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                1 -> return incomeView
                2 -> return inflater.inflate(R.layout.fragment_expenses, container, false)
                3 -> return inflater.inflate(R.layout.fragment_categories, container, false)
                else -> return inflater.inflate(R.layout.fragment_income, container, false)
            }
        }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private val ARG_SECTION_NUMBER = "section_number"

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

            class GetIncomeDataTask internal constructor(fragView: View) : AsyncTask<Void, Void, String>() {
                private val view: WeakReference<View> = WeakReference(fragView)
                private lateinit var viewAdapter: RecyclerView.Adapter<*>
                private lateinit var viewManager: LayoutManager
                private val data: MutableList<Income> = mutableListOf<Income>()

                override fun doInBackground(vararg params: Void): String? {
                    if (data.size == 0) {
                        val wsh = WebServiceHandler()
                        return wsh.performGetCall("php/get_income.php")
                    } else {
                        return ""
                    }
                }

                override fun onPostExecute(success: String?) {
                    if (!success!!.isEmpty()) {
                        // Convert JSON response to List<Income>
                        val resp = JSONArray(success)
                        for (inc in 0 until resp.length()) {
                            val item = resp.getJSONObject(inc)
                            data.add(Income(item.getString("id"), item.getString("date"), item.getString("amount"), item.getString("comment")))
                        }

                        viewManager = LinearLayoutManager(view.get()!!.context)
                        viewAdapter = IncomeAdapter(data) { incItem: Income -> incomeItemClicked(incItem) }

                        val recyclerView = view.get()!!.findViewById(R.id.rvIncome) as RecyclerView
                        // use this setting to improve performance if you know that changes
                        // in content do not change the layout size of the RecyclerView
                        recyclerView.setHasFixedSize(true)

                        // use a linear layout manager
                        recyclerView.layoutManager = viewManager

                        // specify an viewAdapter (see also next example)
                        recyclerView.adapter = viewAdapter
                    }
                }

                private fun incomeItemClicked(incItem: Income) {
                    Toast.makeText(view.get()!!.context, "Clicked: ${incItem.id}", Toast.LENGTH_LONG).show()

                    // Launch second activity, pass part ID as string parameter
//            val showDetailActivityIntent = Intent(this, PartDetailActivity::class.java)
//            showDetailActivityIntent.putExtra(Intent.EXTRA_TEXT, incItem.id.toString())
//            startActivity(showDetailActivityIntent)
                }
            }
        }
    }
}
