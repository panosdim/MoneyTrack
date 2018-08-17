package com.panosdim.moneytrack

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject

data class Category(var id: String? = null, var category: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString()) {
    }

    override fun toString(): String {
        return category
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(category)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {
            json.put("id", id)
            json.put("category", category)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json;
    }
}