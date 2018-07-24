package com.panosdim.moneytrack

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.income_row.view.*

class IncomeAdapter (val incomeItemList: List<Income>, val clickListener: (Income) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.income_row, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        (holder as IncomeViewHolder).bind(incomeItemList[position], clickListener)
    }

    override fun getItemCount() = incomeItemList.size

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(inc: Income, clickListener: (Income) -> Unit) {
            itemView.tvDate.text = inc.date
            itemView.tvSalary.text = inc.salary
            itemView.tvComment.text = inc.comment
            itemView.setOnClickListener { clickListener(inc)}
        }
    }
}
