package com.panosdim.moneytrack.model

import com.panosdim.moneytrack.SortDirection
import com.panosdim.moneytrack.SortField
import com.panosdim.moneytrack.expensesList

object ExpensesSort {
    var field: SortField = SortField.DATE
    var direction: SortDirection = SortDirection.DESC

    fun sort() {
        when (field) {
            SortField.DATE -> when (direction) {
                SortDirection.ASC -> expensesList.sortBy { it.date }
                SortDirection.DESC -> expensesList.sortByDescending { it.date }
            }

            SortField.AMOUNT -> when (direction) {
                SortDirection.ASC -> expensesList.sortBy { it.amount.toDouble() }
                SortDirection.DESC -> expensesList.sortByDescending { it.amount.toDouble() }
            }

            SortField.CATEGORY -> when (direction) {
                SortDirection.ASC -> expensesList.sortBy { it.category }
                SortDirection.DESC -> expensesList.sortByDescending { it.category }
            }

            SortField.COMMENT -> when (direction) {
                SortDirection.ASC -> expensesList.sortBy { it.comment }
                SortDirection.DESC -> expensesList.sortByDescending { it.comment }
            }
        }
    }
}