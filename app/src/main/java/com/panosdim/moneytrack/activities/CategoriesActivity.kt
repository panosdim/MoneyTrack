package com.panosdim.moneytrack.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.CategoryAdapter
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.dialogs.CategoryDialog
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import com.panosdim.moneytrack.utils.loginWithStoredCredentials
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException


class CategoriesActivity : AppCompatActivity(), RefreshView {
    private lateinit var categoryViewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = getString(R.string.categories)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        categoryViewAdapter =
            CategoryAdapter(categoriesList) { catItem: Category -> categoryItemClicked(catItem) }

        rvCategories.setHasFixedSize(true)
        rvCategories.layoutManager = LinearLayoutManager(this)
        rvCategories.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        rvCategories.adapter = categoryViewAdapter

        addNewCategory.setOnClickListener {
            CategoryDialog(
                this,
                this
            ).show()
        }
    }

    private fun categoryItemClicked(catItem: Category) {
        CategoryDialog(this, this, catItem).show()
    }

    override fun refreshView() {
        categoriesList.sortByDescending { it.count }
        rvCategories.adapter?.notifyDataSetChanged()
    }

    private suspend fun downloadCategories() {
        val response = repository.getAllCategories()
        categoriesList.clear()
        categoriesList.addAll(response.data)
        categoriesList.sortByDescending { it.count }
    }

    override fun onResume() {
        super.onResume()
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            try {
                downloadCategories()
            } catch (e: HttpException) {
                loginWithStoredCredentials(this@CategoriesActivity, ::downloadCategories)
            }
        }
    }
}
