package com.panosdim.moneytrack

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.panosdim.moneytrack.adapters.TabAdapter
import com.panosdim.moneytrack.dialogs.*
import com.panosdim.moneytrack.fragments.CategoriesFragment
import com.panosdim.moneytrack.fragments.DashboardFragment
import com.panosdim.moneytrack.fragments.ExpensesFragment
import com.panosdim.moneytrack.fragments.IncomeFragment
import com.panosdim.moneytrack.model.ExpensesFilters
import com.panosdim.moneytrack.model.IncomeFilters
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_expenses.*
import kotlinx.android.synthetic.main.fragment_income.*


class MainActivity : AppCompatActivity() {
    private val dashboardFragment = DashboardFragment()
    private val incomeFragment = IncomeFragment()
    private val expensesFragment = ExpensesFragment()
    private val categoriesFragment = CategoriesFragment()
    private val params = CoordinatorLayout.LayoutParams(
        CoordinatorLayout.LayoutParams.WRAP_CONTENT,
        CoordinatorLayout.LayoutParams.WRAP_CONTENT
    )
    private var margin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomBar()

        margin = resources.getDimension(R.dimen.layout_margin).toInt()

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
                        hideBottomAppBar()
                    }
                    3 -> {
                        addNew.show()
                        hideBottomAppBar()
                    }
                    else -> {
                        invalidateOptionsMenu()
                        addNew.show()
                        showBottomAppBar()
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

        addNew.hide()
        bottomAppBar.isVisible = false
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_bar_menu, menu)
        when (tabs.selectedTabPosition) {
            1 -> toggleSortIconColor(incomeFragment.crdSortIncome.isVisible)
            2 -> toggleSortIconColor(expensesFragment.crdSortExpenses.isVisible)
        }
        updateMenuIcons()

        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupBottomBar() {
        setSupportActionBar(bottomAppBar)
        bottomAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_filter -> {
                    when (tabs.selectedTabPosition) {
                        1 -> IncomeFilterDialog(this, incomeFragment).show()
                        2 -> ExpensesFilterDialog(this, expensesFragment).show()
                    }
                    true
                }

                R.id.action_sort -> {
                    when (tabs.selectedTabPosition) {
                        1 -> {
                            incomeFragment.crdSortIncome.isVisible =
                                !incomeFragment.crdSortIncome.isVisible
                            toggleSortIconColor(incomeFragment.crdSortIncome.isVisible)
                        }
                        2 -> {
                            expensesFragment.crdSortExpenses.isVisible =
                                !expensesFragment.crdSortExpenses.isVisible
                            toggleSortIconColor(expensesFragment.crdSortExpenses.isVisible)
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

    private fun hideBottomAppBar() {
        bottomAppBar.isVisible = false
        params.setMargins(
            0,
            margin, 0, 0
        )
        viewPager.layoutParams = params
    }

    private fun showBottomAppBar() {
        params.setMargins(
            0,
            margin,
            0,
            margin
        )
        viewPager.layoutParams = params
        bottomAppBar.isVisible = true
    }

    private fun toggleSortIconColor(visibility: Boolean) {
        bottomAppBar.menu.findItem(R.id.action_sort).icon = if (visibility) {
            getDrawable(R.drawable.ic_sort_by_alpha_blue_a700_24dp)
        } else {
            getDrawable(R.drawable.ic_sort_by_alpha_white_24dp)
        }
    }

    fun updateMenuIcons() {
        when (tabs.selectedTabPosition) {
            1 -> {
                if (IncomeFilters.isFiltersSet) {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_blue_a700_24dp)
                } else {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_white_24dp)
                }
            }
            2 -> {
                if (ExpensesFilters.isFiltersSet) {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_blue_a700_24dp)
                } else {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_white_24dp)
                }
            }
        }

    }
}
