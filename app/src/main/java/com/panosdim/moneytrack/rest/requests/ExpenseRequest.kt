package com.panosdim.moneytrack.rest.requests

data class ExpenseRequest(
    val date: String,
    val amount: String,
    val comment: String,
    val category: String
)