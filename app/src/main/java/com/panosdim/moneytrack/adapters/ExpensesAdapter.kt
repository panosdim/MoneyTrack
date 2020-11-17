package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.moneyFormat
import kotlinx.android.synthetic.main.row_expense.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpensesAdapter(
        private val expensesList: List<Expense>,
        private val categoriesList: List<Category>,
        private val clickListener: (Expense) -> Unit
) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.row_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        val category = categoriesList.find { it.id == expensesList[position].category }?.category
                ?: ""
        (holder as ExpenseViewHolder).bind(expensesList[position], category, clickListener)
    }

    override fun getItemCount() = expensesList.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(exp: Expense, category: String, clickListener: (Expense) -> Unit) {
            val dateFormatter = DateTimeFormatter.ofPattern("E dd-MM-yyyy")
            val date = LocalDate.parse(exp.date)

            itemView.expDate.text = date.format(dateFormatter)
            itemView.expAmount.text = moneyFormat(exp.amount)
            itemView.expComment.text = exp.comment
            itemView.expCategory.text = category
            itemView.setOnClickListener { clickListener(exp) }
        }
    }
}