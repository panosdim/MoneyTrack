package com.panosdim.moneytrack.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.RefreshView
import com.panosdim.moneytrack.repository
import com.panosdim.moneytrack.rest.requests.CategoryRequest
import kotlinx.android.synthetic.main.dialog_category.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException


class CategoryDialog(
    private var _context: Context,
    private var listener: RefreshView,
    private var category: Category? = null
) :
    Dialog(_context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_category)
        val windowProps = window?.attributes

        windowProps?.gravity = Gravity.BOTTOM
        windowProps?.width = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = windowProps
        this.setCanceledOnTouchOutside(false)

        btnCancel.setOnClickListener {
            this.hide()
        }

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            category?.let {
                scope.launch {
                    prgIndicator.visibility = View.VISIBLE
                    btnDelete.isEnabled = false
                    try {
                        val response = repository.deleteCategory(it.id!!)
                        when (response.code()) {
                            204 -> {
                                categoriesList.remove(it)
                                listener.refreshView()
                                this@CategoryDialog.hide()
                            }
                            409 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting category. Category is connected with an expense.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                            404 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting category. Category not found.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                            403 -> {
                                Toast.makeText(
                                    _context,
                                    "Error deleting category. Category not belong to you.",
                                    Toast.LENGTH_LONG
                                ).show()
                                prgIndicator.visibility = View.GONE
                                btnDelete.isEnabled = true
                            }
                        }
                    } catch (ex: HttpException) {
                        Toast.makeText(
                            _context,
                            "Error deleting category.",
                            Toast.LENGTH_LONG
                        ).show()
                        prgIndicator.visibility = View.GONE
                        btnDelete.isEnabled = true
                    }
                }
            }
        }

        category?.let {
            tvTitle.text = _context.getString(R.string.edit_category)
            tvCategory.setText(it.category)
        } ?: kotlin.run {
            btnDelete.visibility = View.GONE
        }
    }

    private fun validateInputs() {
        // Reset errors.
        tvCategory.error = null

        // Store values.
        val catName = tvCategory.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid category.
        if (catName.isEmpty()) {
            tvCategory.error = _context.getString(R.string.error_field_required)
            focusView = tvCategory
            cancel = true
        }

        category?.let {
            if (catName != category!!.category && categoriesList.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                tvCategory.error = _context.getString(R.string.error_same_name_conflict)
                focusView = tvCategory
                cancel = true
            }
        } ?: kotlin.run {
            if (categoriesList.find {
                    it.category.equals(
                        catName,
                        true
                    )
                } != null) {
                tvCategory.error = _context.getString(R.string.error_same_name_conflict)
                focusView = tvCategory
                cancel = true
            }
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            saveCategory()
        }
    }

    private fun saveCategory() {
        prgIndicator.visibility = View.VISIBLE
        btnSave.isEnabled = false

        val data = CategoryRequest(
            tvCategory.text.toString()
        )

        category?.let {
            // Check if we change something in the object
            if (it.category == tvCategory.text.toString()) {
                this@CategoryDialog.hide()
                return
            }

            // Update Category
            scope.launch {
                try {
                    val response = repository.updateCategory(it.id!!, data)
                    val index = categoriesList.indexOfFirst { (id) -> id == response.data.id }
                    categoriesList[index] = response.data
                    listener.refreshView()
                    this@CategoryDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error updating category.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        } ?: kotlin.run {
            // Save Category
            scope.launch {
                try {
                    val response = repository.createNewCategory(data)
                    categoriesList.add(response.data)
                    listener.refreshView()
                    this@CategoryDialog.hide()
                } catch (ex: HttpException) {
                    Toast.makeText(
                        _context,
                        "Error creating new category.",
                        Toast.LENGTH_LONG
                    ).show()
                    prgIndicator.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }
}