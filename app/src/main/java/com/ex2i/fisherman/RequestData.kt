package com.ex2i.fisherman

import android.os.Parcel
import android.os.Parcelable

data class RequestData(val name: String, var image: Int) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RequestData> {
        override fun createFromParcel(parcel: Parcel): RequestData {
            return RequestData(parcel)
        }

        override fun newArray(size: Int): Array<RequestData?> {
            return arrayOfNulls(size)
        }
    }
}
