package com.panosdim.moneytrack.model

import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.unaccent
import java.time.LocalDate

object ExpensesFilters {
    var filterDate: Pair<LocalDate, LocalDate>? = null
        set(value) {
            field = value
            setFilter()
        }
    var filterComment: String? = null
        set(value) {
            field = value
            setFilter()
        }
    var filterCategory: MutableList<Category>? = null
        set(value) {
            field = value
            setFilter()
        }
    var isFiltersSet = false
    private var originalExpensesList = mutableListOf<Expense>()

    init {
        originalExpensesList.clear()
        originalExpensesList.addAll(expensesList)
    }

    private fun setFilter() {
        val prevValue = isFiltersSet
        isFiltersSet = filterComment != null || filterDate != null || filterCategory != null
        if (!prevValue && isFiltersSet) {
            originalExpensesList.clear()
            originalExpensesList.addAll(expensesList)
        }
        if (prevValue && !isFiltersSet) {
            expensesList.clear()
            expensesList.addAll(originalExpensesList)
            originalExpensesList.clear()
        }
    }

    fun clearFilters() {
        isFiltersSet = false
        filterComment = null
        filterDate = null
        filterCategory = null
    }

    fun filterExpenses() {
        if (isFiltersSet) {
            expensesList.clear()
            expensesList.addAll(originalExpensesList)
        }

        // Date Search
        filterDate?.let { (first, second) ->
            expensesList.retainAll {
                val date = LocalDate.parse(it.date)
                (date.isAfter(first) || date.isEqual(first)) && (date.isBefore(second) || date.isEqual(
                    second
                ))
            }
        }

        // Category Search
        filterCategory?.let { categories: MutableList<Category> ->
            val ids = categories.map { it.id }
            expensesList.retainAll {
                ids.contains(it.category)
            }
        }

        // Comment Search
        filterComment?.let { filter: String ->
            expensesList.retainAll {
                it.comment.unaccent().contains(filter, ignoreCase = true)
            }
        }
    }
}