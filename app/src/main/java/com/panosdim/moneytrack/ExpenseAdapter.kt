package com.panosdim.moneytrack

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.expense_row.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class ExpenseAdapter (private val expenseItemList: List<Expense>, private val clickListener: (Expense) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.expense_row, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        (holder as ExpenseViewHolder).bind(expenseItemList[position], clickListener)
    }

    override fun getItemCount() = expenseItemList.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(exp: Expense, clickListener: (Expense) -> Unit) {
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,###.00 €", symbols)

            itemView.date.text = exp.date
            itemView.expense.text = moneyFormat.format(exp.amount.toDouble())
            itemView.category.text = exp.category
            itemView.comment.text = exp.comment
            itemView.setOnClickListener { clickListener(exp)}
        }
    }
}
