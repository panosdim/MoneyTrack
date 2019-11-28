package com.panosdim.moneytrack.rest.responses

import com.panosdim.moneytrack.model.Income

data class AllIncomeResponse(
    val data: List<Income> = ArrayList()
)