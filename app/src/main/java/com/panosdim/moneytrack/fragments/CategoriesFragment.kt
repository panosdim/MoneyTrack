package com.panosdim.moneytrack.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.LoginActivity
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.CategoryAdapter
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.dialogs.CategoryDialog
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import kotlinx.android.synthetic.main.fragment_categories.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CategoriesFragment : Fragment(), RefreshView {
    private lateinit var categoriesView: View
    private lateinit var categoryViewAdapter: RecyclerView.Adapter<*>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        categoriesView = inflater.inflate(R.layout.fragment_categories, container, false)
        categoryViewAdapter =
            CategoryAdapter(categoriesList) { catItem: Category -> categoryItemClicked(catItem) }

        val categoriesRV = categoriesView.rvCategories
        categoriesRV.setHasFixedSize(true)
        categoriesRV.layoutManager = LinearLayoutManager(categoriesView.context)
        categoriesRV.addItemDecoration(
            DividerItemDecoration(
                categoriesRV.context,
                DividerItemDecoration.VERTICAL
            )
        )

        categoriesRV.adapter = categoryViewAdapter

        return categoriesView
    }

    private fun categoryItemClicked(catItem: Category) {
        CategoryDialog(requireContext(), this, catItem).show()
    }

    override fun refreshView() {
        categoriesList.sortByDescending { it.count }
        categoriesView.rvCategories?.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                val response = repository.getAllCategories()
                categoriesList.clear()
                categoriesList.addAll(response.data)
                categoriesList.sortByDescending { it.count }
            } catch (e: HttpException) {
                val intent = Intent(activity, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

}
