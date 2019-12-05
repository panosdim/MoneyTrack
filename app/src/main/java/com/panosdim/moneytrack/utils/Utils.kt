package com.panosdim.moneytrack.utils

import android.content.Context
import android.content.Intent
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.activities.LoginActivity
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.expensesList
import com.panosdim.moneytrack.incomeList
import retrofit2.HttpException
import java.text.Normalizer

val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

suspend fun downloadData(context: Context) {
    try {
        val responseIncome = App.repository.getAllIncome()
        incomeList.clear()
        incomeList.addAll(responseIncome.data)

        val responseExpenses = App.repository.getAllExpenses()
        expensesList.clear()
        expensesList.addAll(responseExpenses.data)

        val responseCategories = App.repository.getAllCategories()
        categoriesList.clear()
        categoriesList.addAll(responseCategories.data)
        categoriesList.sortByDescending { it.count }
    } catch (e: HttpException) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}