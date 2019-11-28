package com.panosdim.moneytrack.model

data class Expense(
    var id: Int? = null,
    var date: String,
    var amount: Float,
    var category: Int,
    var comment: String
)