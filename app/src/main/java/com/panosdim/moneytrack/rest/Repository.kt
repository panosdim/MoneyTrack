package com.panosdim.moneytrack.rest

import com.panosdim.moneytrack.rest.requests.CategoryRequest
import com.panosdim.moneytrack.rest.requests.ExpenseRequest
import com.panosdim.moneytrack.rest.requests.IncomeRequest
import com.panosdim.moneytrack.rest.requests.LoginRequest

class Repository {
    private var client: Webservice = webservice

    suspend fun login(loginRequest: LoginRequest) = client.login(loginRequest)
    suspend fun checkSession() = client.user()

    suspend fun getAllIncome() = client.income()
    suspend fun createNewIncome(newIncome: IncomeRequest) = client.income(newIncome)
    suspend fun deleteIncome(id: Int) = client.income(id)
    suspend fun updateIncome(id: Int, updatedIncome: IncomeRequest) =
        client.income(id, updatedIncome)

    suspend fun getAllExpenses() = client.expense()
    suspend fun createNewExpense(newExpense: ExpenseRequest) = client.expense(newExpense)
    suspend fun deleteExpense(id: Int) = client.expense(id)
    suspend fun updateExpense(id: Int, updatedExpense: ExpenseRequest) =
        client.expense(id, updatedExpense)

    suspend fun getAllCategories() = client.category()
    suspend fun createNewCategory(newCategory: CategoryRequest) = client.category(newCategory)
    suspend fun deleteCategory(id: Int) = client.category(id)
    suspend fun updateCategory(id: Int, updatedCategory: CategoryRequest) =
        client.category(id, updatedCategory)
}