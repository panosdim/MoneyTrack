package com.panosdim.moneytrack.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.adapters.CategoriesAdapter
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.utils.generateTextWatcher
import com.panosdim.moneytrack.viewmodel.CategoriesViewModel
import kotlinx.android.synthetic.main.dialog_category.view.*
import kotlinx.android.synthetic.main.fragment_categories.*
import kotlinx.android.synthetic.main.fragment_categories.view.*

class CategoriesFragment : Fragment() {
    private val categoriesViewAdapter =
        CategoriesAdapter(mutableListOf()) { categoryItem: Category ->
            categoryItemClicked(
                categoryItem
            )
        }
    private lateinit var dialog: BottomSheetDialog
    private lateinit var dialogView: View
    private var category: Category? = null
    private val viewModel: CategoriesViewModel by viewModels()
    private val textWatcher = generateTextWatcher(::validateForm)

    private fun categoryItemClicked(categoryItem: Category) {
        category = categoryItem
        showForm(category)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            val data = list.toMutableList()
            data.sortByDescending { it.count }
            rvCategories.adapter =
                CategoriesAdapter(data) { categoryItem: Category -> categoryItemClicked(categoryItem) }
            (rvCategories.adapter as CategoriesAdapter).notifyDataSetChanged()
        }

        catSwipeRefresh.setOnRefreshListener {
            viewModel.refreshCategories()
            catSwipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_categories, container, false)

        dialog = BottomSheetDialog(requireContext())
        dialogView = inflater.inflate(R.layout.dialog_category, container, false)
        dialog.setContentView(dialogView)

        val rvCategories = root.rvCategories
        rvCategories.setHasFixedSize(true)
        rvCategories.layoutManager = LinearLayoutManager(root.context)
        rvCategories.adapter = categoriesViewAdapter

        root.addNewCategory.setOnClickListener {
            category = null
            showForm(category)
        }

        dialogView.categoryName.setOnEditorActionListener { _, actionId, event ->
            if (isFormValid() && (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE)) {
                category?.let {
                    updateCategory(it)
                } ?: kotlin.run {
                    saveCategory()
                }
            }
            false
        }

        dialogView.saveCategory.setOnClickListener {
            category?.let {
                updateCategory(it)
            } ?: kotlin.run {
                saveCategory()
            }
        }

        dialogView.deleteCategory.setOnClickListener {
            deleteCategory()
        }

        return root
    }

    private fun deleteCategory() {
        category?.let {
            viewModel.removeCategory(it).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dialog.hide()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteCategory.isEnabled = true
                            dialogView.saveCategory.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteCategory.isEnabled = true
                            dialogView.saveCategory.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteCategory.isEnabled = false
                            dialogView.saveCategory.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun saveCategory() {
        val newCategory = Category(
            null,
            dialogView.categoryName.text.toString(),
            0
        )

        viewModel.addCategory(newCategory).observe(viewLifecycleOwner) { resource ->
            if (resource != null) {
                when (resource) {
                    is Resource.Success -> {
                        dialog.hide()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteCategory.isEnabled = true
                        dialogView.saveCategory.isEnabled = true
                    }
                    is Resource.Error -> {
                        Toast.makeText(
                            requireContext(),
                            resource.message,
                            Toast.LENGTH_LONG
                        ).show()
                        dialogView.prgIndicator.visibility = View.GONE
                        dialogView.deleteCategory.isEnabled = true
                        dialogView.saveCategory.isEnabled = true
                    }
                    is Resource.Loading -> {
                        dialogView.prgIndicator.visibility = View.VISIBLE
                        dialogView.deleteCategory.isEnabled = false
                        dialogView.saveCategory.isEnabled = false
                    }
                }
            }
        }
    }

    private fun updateCategory(category: Category) {
        // Check if we change something in the object
        if (category.category == dialogView.categoryName.text.toString()) {
            dialog.hide()
        } else {
            // Update Category
            category.category = dialogView.categoryName.text.toString()

            viewModel.updateCategory(category).observe(viewLifecycleOwner) { resource ->
                if (resource != null) {
                    when (resource) {
                        is Resource.Success -> {
                            dialog.hide()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteCategory.isEnabled = true
                            dialogView.saveCategory.isEnabled = true
                        }
                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_LONG
                            ).show()
                            dialogView.prgIndicator.visibility = View.GONE
                            dialogView.deleteCategory.isEnabled = true
                            dialogView.saveCategory.isEnabled = true
                        }
                        is Resource.Loading -> {
                            dialogView.prgIndicator.visibility = View.VISIBLE
                            dialogView.deleteCategory.isEnabled = false
                            dialogView.saveCategory.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun showForm(category: Category?) {
        dialogView.prgIndicator.visibility = View.GONE
        dialogView.saveCategory.isEnabled = true
        dialogView.deleteCategory.isEnabled = true

        dialogView.categoryName.removeTextChangedListener(textWatcher)
        dialogView.categoryName.error = null

        if (category == null) {
            dialogView.categoryName.addTextChangedListener(textWatcher)
            dialogView.categoryName.setText("")
            dialogView.deleteCategory.visibility = View.GONE
            dialogView.saveCategory.setText(R.string.save)
            dialogView.categoryName.requestFocus()
        } else {
            dialogView.categoryName.setText(category.category)
            dialogView.categoryName.clearFocus()
            dialogView.deleteCategory.visibility = View.VISIBLE
            dialogView.saveCategory.setText(R.string.update)
            dialogView.categoryName.addTextChangedListener(textWatcher)
        }

        dialog.show()
    }

    private fun validateForm() {
        val categoryName = dialogView.categoryName
        val saveCategory = dialogView.saveCategory
        saveCategory.isEnabled = true
        categoryName.error = null

        // Store values.
        val catName = categoryName.text.toString()

        if (catName.isEmpty()) {
            categoryName.error = getString(R.string.error_field_required)
            saveCategory.isEnabled = false
        }

        // Check if existing category has the same name
        category?.let {
            if (catName != category!!.category && viewModel.categories.value?.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                categoryName.error = getString(R.string.error_same_name_conflict)
                saveCategory.isEnabled = false
            }
        } ?: kotlin.run {
            if (viewModel.categories.value?.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                categoryName.error = getString(R.string.error_same_name_conflict)
                saveCategory.isEnabled = false
            }
        }
    }

    private fun isFormValid(): Boolean {
        return dialogView.categoryName.error == null
    }
}