package com.panosdim.moneytrack

import android.app.Application

val prefs: Prefs by lazy {
    App.prefs!!
}

val categoriesList: MutableList<Category> by lazy {
    App.categories
}

val incomeList: MutableList<Income> by lazy {
    App.income
}

val expensesList: MutableList<Expense> by lazy {
    App.expenses
}

class App : Application() {
    companion object {
        var prefs: Prefs? = null
        var categories: MutableList<Category> = mutableListOf()
        var expenses: MutableList<Expense> = mutableListOf()
        var income: MutableList<Income> = mutableListOf()
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        super.onCreate()
    }
}