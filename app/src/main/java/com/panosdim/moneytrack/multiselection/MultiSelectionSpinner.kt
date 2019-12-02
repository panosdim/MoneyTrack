package com.panosdim.moneytrack.multiselection

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import androidx.appcompat.widget.AppCompatSpinner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.panosdim.moneytrack.model.Category
import java.util.*


class MultiSelectionSpinner : AppCompatSpinner, OnMultiChoiceClickListener {
    var items: MutableList<Category>? = null
        set(value) {
            field = value
            selection = BooleanArray(value!!.size)
            adapter.clear()
            adapter.add("")
            Arrays.fill(selection!!, false)
        }
    private var selection: BooleanArray? = null
    private var adapter: ArrayAdapter<Any>

    constructor(context: Context) : super(context) {
        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item
        )
        super.setAdapter(adapter)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item
        )
        super.setAdapter(adapter)
    }

    override fun onClick(
        dialog: DialogInterface,
        which: Int,
        isChecked: Boolean
    ) {
        if (selection != null && which < selection!!.size) {
            selection!![which] = isChecked
            adapter.clear()
            adapter.add(buildSelectedItemString())
        } else {
            throw IllegalArgumentException(
                "Argument 'which' is out of bounds."
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun performClick(): Boolean {
        val dialog = MaterialAlertDialogBuilder(context)
        val itemNames = arrayOfNulls<String>(items!!.size)
        for (i in items!!.indices) {
            itemNames[i] = items!![i].category
        }
        dialog.setMultiChoiceItems(itemNames, selection, this)
        dialog.setPositiveButton("OK") { _, _ ->
            // Do nothing
        }
        dialog.setNeutralButton("Clear") { _, _ ->
            for (i in selection!!.indices) {
                selection!![i] = false
            }
            adapter.clear()
            adapter.add(buildSelectedItemString())
        }

        dialog.show()
        return true
    }

    override fun setAdapter(adapter: SpinnerAdapter?) {
        throw RuntimeException(
            "setAdapter is not supported by MultiSelectSpinner."
        )
    }

    fun setSelection(selection: MutableList<Category>) {
        for (i in this.selection!!.indices) {
            this.selection!![i] = false
        }
        for (sel in selection) {
            for (j in items!!.indices) {
                if (items!![j].category == sel.category) {
                    this.selection!![j] = true
                }
            }
        }
        adapter.clear()
        adapter.add(buildSelectedItemString())
    }

    private fun buildSelectedItemString(): String {
        val sb = StringBuilder()
        var foundOne = false
        for (i in items!!.indices) {
            if (selection!![i]) {
                if (foundOne) {
                    sb.append(", ")
                }
                foundOne = true
                sb.append(items!![i].category)
            }
        }
        return sb.toString()
    }

    val selectedItems: MutableList<Category>
        get() {
            val selectedItems =
                ArrayList<Category>()
            for (i in items!!.indices) {
                if (selection!![i]) {
                    selectedItems.add(items!![i])
                }
            }
            return selectedItems
        }
}