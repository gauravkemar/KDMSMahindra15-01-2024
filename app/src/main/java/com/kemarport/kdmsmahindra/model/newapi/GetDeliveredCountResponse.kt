package com.kemarport.kdmsmahindra.model.newapi

data class GetDeliveredCountResponse(
    val vehicleMonthsCount: Int,
    val vehicleMoreThanMonthCount: Int,
    val vehicleTodaysCount: Int,
    val vehicleWeeksCount: Int,
    val vehicleYesterdayCount: Int
)