package com.panosdim.moneytrack.network

import org.json.JSONObject

fun checkForActiveSession(mCallback: (result: String) -> Unit) {
    GetJsonData(mCallback).execute("php/session.php")
}

fun login(mCallback: (result: String) -> Unit, credentials: JSONObject) {
    PutJsonData(mCallback, "php/login.php").execute(credentials)
}

fun logout(mCallback: (result: String) -> Unit) {
    GetJsonData(mCallback).execute("php/logout.php")
}

fun getExpenses(mCallback: (result: String) -> Unit) {
    GetJsonData(mCallback).execute("php/get_expense.php")
}

fun getIncome(mCallback: (result: String) -> Unit) {
    GetJsonData(mCallback).execute("php/get_income.php")
}

fun getCategories(mCallback: (result: String) -> Unit) {
    GetJsonData(mCallback).execute("php/get_categories.php")
}

fun deleteCategory(mCallback: (result: String) -> Unit, category: JSONObject) {
    PutJsonData(mCallback, "php/delete_category.php").execute(category)
}

fun saveCategory(mCallback: (result: String) -> Unit, category: JSONObject) {
    PutJsonData(mCallback, "php/save_category.php").execute(category)
}

fun deleteExpense(mCallback: (result: String) -> Unit, expense: JSONObject) {
    PutJsonData(mCallback, "php/delete_expense.php").execute(expense)
}

fun saveExpense(mCallback: (result: String) -> Unit, expense: JSONObject) {
    PutJsonData(mCallback, "php/save_expense.php").execute(expense)
}

fun deleteIncome(mCallback: (result: String) -> Unit, income: JSONObject) {
    PutJsonData(mCallback, "php/delete_income.php").execute(income)
}

fun saveIncome(mCallback: (result: String) -> Unit, income: JSONObject) {
    PutJsonData(mCallback, "php/save_income.php").execute(income)
}

