package com.amazing.mvvm.model.data.schedule

import com.google.gson.annotations.SerializedName
import java.util.*

data class ScheduleData(
    @SerializedName("available") val available: List<Period?>?,
    @SerializedName("booked") val booked: List<Period?>?
) {
    data class Period(
        @SerializedName("end") val end: Date?,
        @SerializedName("start") val start: Date?
    )
}
