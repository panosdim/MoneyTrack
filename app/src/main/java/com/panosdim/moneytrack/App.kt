package com.panosdim.moneytrack

import android.app.Application
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.rest.Repository

val prefs: Prefs by lazy {
    App.prefs!!
}

val repository: Repository by lazy {
    App.repository
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
        var repository = Repository()
        var categories: MutableList<Category> = mutableListOf()
        var expenses: MutableList<Expense> = mutableListOf()
        var income: MutableList<Income> = mutableListOf()
    }

    override fun onCreate() {
        prefs = Prefs(applicationContext)
        super.onCreate()
    }
}