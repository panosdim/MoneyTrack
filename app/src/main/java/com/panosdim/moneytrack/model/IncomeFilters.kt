package com.panosdim.moneytrack.model

import com.panosdim.moneytrack.incomeList
import com.panosdim.moneytrack.unaccent
import java.time.LocalDate

object IncomeFilters {
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
    var isFiltersSet = false
    private var originalIncomeList = mutableListOf<Income>()

    init {
        originalIncomeList.clear()
        originalIncomeList.addAll(incomeList)
    }

    private fun setFilter() {
        val prevValue = isFiltersSet
        isFiltersSet = filterComment != null || filterDate != null
        if (!prevValue && isFiltersSet) {
            originalIncomeList.clear()
            originalIncomeList.addAll(incomeList)
        }
        if (prevValue && !isFiltersSet) {
            incomeList.clear()
            incomeList.addAll(originalIncomeList)
            originalIncomeList.clear()
        }
    }

    fun clearFilters() {
        isFiltersSet = false
        filterComment = null
        filterDate = null
    }


    fun filterIncome() {
        if (isFiltersSet) {
            incomeList.clear()
            incomeList.addAll(originalIncomeList)
        }

        // Date Search
        filterDate?.let { (first, second) ->
            incomeList.retainAll {
                val date = LocalDate.parse(it.date)
                (date.isAfter(first) || date.isEqual(first)) && (date.isBefore(second) || date.isEqual(
                    second
                ))
            }
        }

        // Comment Search
        filterComment?.let { filter: String ->
            incomeList.retainAll {
                it.comment.unaccent().contains(filter, ignoreCase = true)
            }
        }
    }
}