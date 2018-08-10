package com.panosdim.moneytrack

data class Category(var id: String? = null, var category: String) {
    override fun toString(): String {
        return category
    }
}