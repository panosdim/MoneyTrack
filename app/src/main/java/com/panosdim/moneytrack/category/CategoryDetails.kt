package com.panosdim.moneytrack.category

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.categoriesList
import com.panosdim.moneytrack.CATEGORY_MESSAGE
import com.panosdim.moneytrack.network.deleteCategory
import com.panosdim.moneytrack.network.saveCategory
import kotlinx.android.synthetic.main.activity_category_details.*
import kotlinx.android.synthetic.main.content_category_details.*
import org.json.JSONObject

class CategoryDetails : AppCompatActivity() {

    private var category = Category(category = "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        btnSave.setOnClickListener {
            validateInputs()
        }

        btnDelete.setOnClickListener {
            if (category.id != null) {
                deleteCategory({
                    val res = JSONObject(it)
                    if (res.getBoolean("success")) {
                        val returnIntent = Intent()
                        categoriesList.remove(category)
                        setResult(Activity.RESULT_OK, returnIntent)
                        finish()
                    }

                    Toast.makeText(this, res.getString("message"),
                            Toast.LENGTH_LONG).show()
                }, category.toJson())
            } else {
                Toast.makeText(this, "Category ID was not found",
                        Toast.LENGTH_LONG).show()
            }
        }

        val bundle = intent.extras
        if (bundle != null) {
            category = bundle.getParcelable<Parcelable>(CATEGORY_MESSAGE) as Category
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }

        tvCategory.setText(category.category)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    @SuppressLint("SimpleDateFormat")
    private fun validateInputs() {
        // Reset errors.
        tvCategory.error = null

        // Store values.
        val categoryValue = tvCategory.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid category.
        if (categoryValue.isEmpty()) {
            tvCategory.error = getString(R.string.error_field_required)
            focusView = tvCategory
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            category.category = categoryValue
            saveCategory({
                val res = JSONObject(it)
                if (res.getBoolean("success")) {
                    if (category.id == null) {
                        category.id = res.getJSONObject("data").getString("id")
                        categoriesList.add(category)
                    } else {
                        val index = categoriesList.indexOfFirst { it.id == category.id }
                        categoriesList[index] = category
                    }
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }

                Toast.makeText(this, res.getString("message"),
                        Toast.LENGTH_LONG).show()
            }, category.toJson())
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }
}
