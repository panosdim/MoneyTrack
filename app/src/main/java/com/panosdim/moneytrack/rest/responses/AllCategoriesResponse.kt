package com.panosdim.moneytrack.rest.responses

import com.panosdim.moneytrack.model.Category

data class AllCategoriesResponse(
    val data: List<Category> = ArrayList()
)