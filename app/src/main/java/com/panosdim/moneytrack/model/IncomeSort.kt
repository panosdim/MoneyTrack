package com.panosdim.moneytrack.model

import com.panosdim.moneytrack.SortDirection
import com.panosdim.moneytrack.SortField
import com.panosdim.moneytrack.incomeList

object IncomeSort {
    var field: SortField = SortField.DATE
    var direction: SortDirection = SortDirection.DESC

    fun sort() {
        when (field) {
            SortField.DATE -> when (direction) {
                SortDirection.ASC -> incomeList.sortBy { it.date }
                SortDirection.DESC -> incomeList.sortByDescending { it.date }
            }

            SortField.AMOUNT -> when (direction) {
                SortDirection.ASC -> incomeList.sortBy { it.amount.toDouble() }
                SortDirection.DESC -> incomeList.sortByDescending { it.amount.toDouble() }
            }

            SortField.COMMENT -> when (direction) {
                SortDirection.ASC -> incomeList.sortBy { it.comment }
                SortDirection.DESC -> incomeList.sortByDescending { it.comment }
            }
        }
    }
}
