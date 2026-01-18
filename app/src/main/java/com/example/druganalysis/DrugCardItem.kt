package com.example.druganalysis

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugCardItem(
    val drugName: String,
    val depletion: List<String>,
    val avoid: List<String>,
    val foods: List<String>,
    val found: Boolean
) : Parcelable
