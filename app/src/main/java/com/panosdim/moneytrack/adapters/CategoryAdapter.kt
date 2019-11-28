package com.panosdim.moneytrack.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.model.Category
import kotlinx.android.synthetic.main.row_category.view.*


class CategoryAdapter(
    private val categoryItemList: List<Category>,
    private val clickListener: (Category) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.row_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        (holder as CategoryViewHolder).bind(categoryItemList[position], clickListener)
    }

    override fun getItemCount() = categoryItemList.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(cat: Category, clickListener: (Category) -> Unit) {
            itemView.tvCategory.text = cat.category
            itemView.tvCount.text =
                itemView.context.getString(R.string.number_of_times_used, cat.count)
            itemView.setOnClickListener { clickListener(cat) }
        }
    }
}
