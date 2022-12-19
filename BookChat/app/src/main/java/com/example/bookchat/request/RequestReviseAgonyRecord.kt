package com.example.bookchat.request

import com.google.gson.annotations.SerializedName

data class RequestReviseAgonyRecord(
    @SerializedName("recordTitle")
    val recordTitle :String,
    @SerializedName("recordContent")
    val recordContent :String
)
