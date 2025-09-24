package com.kemarport.kdmsmahindra.model.setgeofence

data class PostGeofenceCoordinatesRequest(
    val DeviceIp: String,
    val coordinates: String,
    val dealerCode: String,
    val locationId: Int,
    val runningLoad: Int
)