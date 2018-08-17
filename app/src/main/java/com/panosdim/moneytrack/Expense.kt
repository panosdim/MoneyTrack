package com.panosdim.moneytrack

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject

data class Expense(var id: String? = null, var date: String, var amount: String, var category: String, var comment: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(date)
        parcel.writeString(amount)
        parcel.writeString(category)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {
            val categoryID = categoriesList.find {
                it.category == category
            }!!.id
            json.put("id", id)
            json.put("date", date)
            json.put("amount", amount)
            json.put("category_id", categoryID)
            json.put("comment", comment)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }

}
