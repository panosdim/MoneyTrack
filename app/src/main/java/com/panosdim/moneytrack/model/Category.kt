package com.panosdim.moneytrack.model

data class Category(var id: Int? = null, var category: String, var count: Int) {
    override fun toString(): String {
        return category
    }
}