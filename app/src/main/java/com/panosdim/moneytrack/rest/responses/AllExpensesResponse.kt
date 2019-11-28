package com.panosdim.moneytrack.rest.responses

import com.panosdim.moneytrack.model.Expense

data class AllExpensesResponse(
    val data: List<Expense> = ArrayList()
)