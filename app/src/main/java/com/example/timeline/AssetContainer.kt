package com.example.timeline

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssetContainer(var id:Int = -1, var description:String):Parcelable