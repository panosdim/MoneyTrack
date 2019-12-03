package com.panosdim.moneytrack.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.google.android.material.datepicker.MaterialDatePicker
import com.panosdim.moneytrack.*
import com.panosdim.moneytrack.model.ExpensesFilters.clearFilters
import com.panosdim.moneytrack.model.ExpensesFilters.filterCategory
import com.panosdim.moneytrack.model.ExpensesFilters.filterComment
import com.panosdim.moneytrack.model.ExpensesFilters.filterDate
import com.panosdim.moneytrack.model.ExpensesFilters.filterExpenses
import com.panosdim.moneytrack.model.ExpensesFilters.isFiltersSet
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.multiselection.MultiSelectionSpinner
import kotlinx.android.synthetic.main.dialog_filter_expenses.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import kotlin.Pair
import androidx.core.util.Pair as APair


class ExpensesFilterDialog(
    private var _context: Context,
    private var listener: RefreshView
) :
    Dialog(_context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_filter_expenses)
        val windowProps = window?.attributes

        windowProps?.gravity = Gravity.BOTTOM
        windowProps?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = windowProps
        this.setCanceledOnTouchOutside(false)

        val mySpinner = tvCategoriesFilter as MultiSelectionSpinner
        mySpinner.items = categoriesList

        btnCancel.setOnClickListener {
            this.hide()
            if (filterDate == null) {
                selectedDateRange = null
            }
        }

        btnSetFilters.setOnClickListener {
            filterComment = if (tvCommentFilter.text.isNullOrEmpty()) {
                null
            } else {
                tvCommentFilter.text.toString().unaccent()
            }

            selectedDateRange?.let {
                val startDate =
                    LocalDate.ofEpochDay(it.first!! / (1000 * 60 * 60 * 24))
                val endDate =
                    LocalDate.ofEpochDay(it.second!! / (1000 * 60 * 60 * 24))
                filterDate = Pair(startDate, endDate)
            } ?: kotlin.run {
                filterDate = null
            }

            filterCategory = if (mySpinner.selectedItems.isEmpty()) {
                null
            } else {
                mySpinner.selectedItems
            }

            filterExpenses()

            val activity = _context as MainActivity
            activity.updateMenuIcons()
            listener.refreshView()
            this.hide()
        }

        btnClearFilters.setOnClickListener {
            selectedDateRange = null
            clearFilters()

            val activity = _context as MainActivity
            activity.updateMenuIcons()

            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch {
                try {
                    val response = repository.getAllExpenses()
                    expensesList.clear()
                    expensesList.addAll(response.data)
                    listener.refreshView()
                    this@ExpensesFilterDialog.hide()
                } catch (e: HttpException) {
                    val intent = Intent(_context, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    _context.startActivity(intent)
                }
            }
            this.hide()
        }

        tvDateFilter.setOnClickListener {
            val builderRange = MaterialDatePicker.Builder.dateRangePicker()
            val pickerRange = selectedDateRange?.let {
                builderRange.setSelection(it)
                builderRange.build()
            } ?: kotlin.run {
                builderRange.build()
            }

            val activity = _context as MainActivity
            pickerRange.show(activity.supportFragmentManager, pickerRange.toString())

            pickerRange.addOnPositiveButtonClickListener {
                selectedDateRange = APair(it.first, it.second)
                val startDate =
                    LocalDate.ofEpochDay(it.first!! / (1000 * 60 * 60 * 24))
                val endDate =
                    LocalDate.ofEpochDay(it.second!! / (1000 * 60 * 60 * 24))
                tvDateFilter.setText(_context.getString(R.string.date_filter, startDate, endDate))
            }

            pickerRange.addOnNegativeButtonClickListener {
                selectedDateRange = null
                tvDateFilter.setText("")
            }
        }

        btnClearFilters.isVisible = isFiltersSet

        filterDate?.let {
            tvDateFilter.setText(_context.getString(R.string.date_filter, it.first, it.second))
        } ?: kotlin.run {
            tvDateFilter.setText("")
        }

        filterComment?.let {
            tvCommentFilter.setText(filterComment)
        } ?: kotlin.run {
            tvCommentFilter.setText("")
        }

        filterCategory?.let {
            mySpinner.setSelection(it)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (filterDate == null) {
            selectedDateRange = null
        }
    }

    companion object {
        var selectedDateRange: APair<Long, Long>? = null
    }
}