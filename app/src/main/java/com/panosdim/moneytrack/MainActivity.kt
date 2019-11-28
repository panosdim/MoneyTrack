package com.panosdim.moneytrack

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.panosdim.moneytrack.adapters.TabAdapter
import com.panosdim.moneytrack.dialogs.CategoryDialog
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.dialogs.IncomeDialog
import com.panosdim.moneytrack.dialogs.IncomeFilterDialog
import com.panosdim.moneytrack.fragments.CategoriesFragment
import com.panosdim.moneytrack.fragments.DashboardFragment
import com.panosdim.moneytrack.fragments.ExpensesFragment
import com.panosdim.moneytrack.fragments.IncomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_expenses.*
import kotlinx.android.synthetic.main.fragment_income.*


class MainActivity : AppCompatActivity() {
    private val dashboardFragment = DashboardFragment()
    private val incomeFragment = IncomeFragment()
    private val expensesFragment = ExpensesFragment()
    private val categoriesFragment = CategoriesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomBar()

        val adapter = TabAdapter(supportFragmentManager)
        adapter.addFragment(dashboardFragment, "Dashboard")
        adapter.addFragment(incomeFragment, "Income")
        adapter.addFragment(expensesFragment, "Expenses")
        adapter.addFragment(categoriesFragment, "Categories")
        viewPager.adapter = adapter

        tabs.setupWithViewPager(viewPager)
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
                // Not needed
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                // Not needed
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                invalidateOptionsMenu()
                when (tab.position) {
                    0 -> {
                        addNew.hide()
                    }
                    else -> {
                        addNew.show()
                    }
                }
            }
        })

        addNew.setOnClickListener {
            when (tabs.selectedTabPosition) {
                1 -> {
                    IncomeDialog(
                        this,
                        adapter.getItem(tabs.selectedTabPosition) as IncomeFragment
                    ).show()
                }
                2 -> {
                    ExpenseDialog(
                        this,
                        adapter.getItem(tabs.selectedTabPosition) as ExpensesFragment
                    ).show()
                }
                3 -> {
                    CategoryDialog(
                        this,
                        adapter.getItem(tabs.selectedTabPosition) as CategoriesFragment
                    ).show()
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_bar_menu, menu)
        when (tabs.selectedTabPosition) {
            0, 3 -> {
                menu.findItem(R.id.action_sort).isVisible = false
                menu.findItem(R.id.action_filter).isVisible = false
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupBottomBar() {
        setSupportActionBar(bottomAppBar)
        bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    Toast.makeText(
                        this, "Logging you out!",
                        Toast.LENGTH_LONG
                    ).show()

                    prefs.password = ""
                    prefs.email = ""
                    prefs.token = ""

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }

                R.id.action_filter -> {
                    when (tabs.selectedTabPosition) {
                        1 -> IncomeFilterDialog(this, incomeFragment).show()
                        2 -> IncomeFilterDialog(this, incomeFragment).show()
                    }
                    true
                }

                R.id.action_sort -> {
                    when (tabs.selectedTabPosition) {
                        1 -> {
                            incomeFragment.crdSortIncome.visibility =
                                if (incomeFragment.crdSortIncome.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                        2 -> {
                            expensesFragment.crdSortExpenses.visibility =
                                if (expensesFragment.crdSortExpenses.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }
                    }
                    true
                }

                else -> {
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    super.onOptionsItemSelected(it)
                }
            }
        }
    }
}
