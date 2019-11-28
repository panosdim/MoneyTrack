package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.model.Income
import kotlinx.android.synthetic.main.row_income.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class IncomeAdapter(
    private val incomeItemList: List<Income>,
    private val clickListener: (Income) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.row_income, parent, false)
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
            val symbols = DecimalFormatSymbols()
            symbols.groupingSeparator = '.'
            symbols.decimalSeparator = ','
            val moneyFormat = DecimalFormat("#,##0.00 €", symbols)
            val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
            val date = LocalDate.parse(inc.date)

            itemView.incDate.text = date.format(dateFormatter)
            itemView.incSalary.text = moneyFormat.format(inc.amount.toDouble())
            itemView.incComment.text = inc.comment
            itemView.setOnClickListener { clickListener(inc) }
        }
    }
}