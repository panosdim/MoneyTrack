package com.panosdim.moneytrack

import android.app.Application
import com.panosdim.moneytrack.category.Category
import com.panosdim.moneytrack.expense.Expense
import com.panosdim.moneytrack.income.Income

const val LOGGEDOUT_MESSAGE = "com.panosdim.moneytrack.LOGGEDOUT"
const val EXPENSE_MESSAGE = "com.panosdim.moneytrack.EXPENSE"
const val INCOME_MESSAGE = "com.panosdim.moneytrack.INCOME"
const val CATEGORY_MESSAGE = "com.panosdim.moneytrack.CATEGORY"
const val OPERATION_MESSAGE = "com.panosdim.moneytrack.OPERATION"

enum class Operations(val code: Int) {
    INCOME(0),
    EXPENSE(1),
    CATEGORY(2),
    FILTER_INCOME(3),
    FILTER_EXPENSE(4),
    FILTER_ADD_EXPENSE(5),
    FILTER_DELETE_EXPENSE(6),
    FILTER_ADD_INCOME(7),
    FILTER_DELETE_INCOME(8)
}

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