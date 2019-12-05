package com.panosdim.moneytrack.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.RC
import com.panosdim.moneytrack.adapters.TabAdapter
import com.panosdim.moneytrack.dialogs.ExpenseDialog
import com.panosdim.moneytrack.dialogs.ExpensesFilterDialog
import com.panosdim.moneytrack.dialogs.IncomeDialog
import com.panosdim.moneytrack.dialogs.IncomeFilterDialog
import com.panosdim.moneytrack.fragments.ExpensesFragment
import com.panosdim.moneytrack.fragments.IncomeFragment
import com.panosdim.moneytrack.model.ExpensesFilters
import com.panosdim.moneytrack.model.IncomeFilters
import com.panosdim.moneytrack.utils.checkForNewVersion
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_expenses.*
import kotlinx.android.synthetic.main.fragment_income.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val incomeFragment = IncomeFragment()
    private val expensesFragment = ExpensesFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomBar()

        val adapter = TabAdapter(supportFragmentManager)
        adapter.addFragment(expensesFragment, "Expenses")
        adapter.addFragment(incomeFragment, "Income")
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
            }
        })

        addNew.setOnClickListener {
            when (tabs.selectedTabPosition) {
                0 -> {
                    ExpenseDialog(
                        this,
                        adapter.getItem(tabs.selectedTabPosition) as ExpensesFragment
                    ).show()
                }
                1 -> {
                    IncomeDialog(
                        this,
                        adapter.getItem(tabs.selectedTabPosition) as IncomeFragment
                    ).show()
                }
            }
        }

        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch() {
            checkForNewVersion(this@MainActivity)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            RC.PERMISSION_REQUEST.code -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val scope = CoroutineScope(Dispatchers.IO)

                    scope.launch() {
                        checkForNewVersion(this@MainActivity)
                    }
                }
                return
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_bar_menu, menu)
        when (tabs.selectedTabPosition) {
            0 -> toggleSortIconColor(expensesFragment.crdSortExpenses.isVisible)
            1 -> toggleSortIconColor(incomeFragment.crdSortIncome.isVisible)
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
                        0 -> ExpensesFilterDialog(this, expensesFragment).show()
                        1 -> IncomeFilterDialog(this, incomeFragment).show()
                    }
                    true
                }

                R.id.action_sort -> {
                    when (tabs.selectedTabPosition) {
                        0 -> {
                            expensesFragment.crdSortExpenses.isVisible =
                                !expensesFragment.crdSortExpenses.isVisible
                            toggleSortIconColor(expensesFragment.crdSortExpenses.isVisible)
                        }
                        1 -> {
                            incomeFragment.crdSortIncome.isVisible =
                                !incomeFragment.crdSortIncome.isVisible
                            toggleSortIconColor(incomeFragment.crdSortIncome.isVisible)
                        }
                    }
                    true
                }

                R.id.action_categories -> {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    startActivity(intent)
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

    private fun toggleSortIconColor(visibility: Boolean) {
        bottomAppBar.menu.findItem(R.id.action_sort).icon = if (visibility) {
            getDrawable(R.drawable.ic_sort_by_alpha_blue_a700_24dp)
        } else {
            getDrawable(R.drawable.ic_sort_by_alpha_white_24dp)
        }
    }

    fun updateMenuIcons() {
        when (tabs.selectedTabPosition) {
            0 -> {
                if (ExpensesFilters.isFiltersSet) {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_blue_a700_24dp)
                } else {
                    bottomAppBar.menu.findItem(R.id.action_filter).icon =
                        getDrawable(R.drawable.ic_filter_list_white_24dp)
                }
            }
            1 -> {
                if (IncomeFilters.isFiltersSet) {
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
