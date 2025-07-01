package com.example.composesample.presentation.example.component.system.platform.intent

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ParcelableDataClass(
    val name: String,
    val age: Int,
    val isMale: Boolean,
    val address: String,
) : Parcelable