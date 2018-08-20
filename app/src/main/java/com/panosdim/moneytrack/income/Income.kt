package com.panosdim.moneytrack.income

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONException
import org.json.JSONObject

data class Income (var id: String? = null, var date: String, var salary: String, var comment: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(date)
        parcel.writeString(salary)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Income> {
        override fun createFromParcel(parcel: Parcel): Income {
            return Income(parcel)
        }

        override fun newArray(size: Int): Array<Income?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {
            json.put("id", id)
            json.put("date", date)
            json.put("amount", salary)
            json.put("comment", comment)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }
}