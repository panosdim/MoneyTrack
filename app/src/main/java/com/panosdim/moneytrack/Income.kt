package com.panosdim.moneytrack

import android.os.Parcel
import android.os.Parcelable
data class Income (val id: String, val date: String, val salary: String, val comment: String) : Parcelable {
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
}