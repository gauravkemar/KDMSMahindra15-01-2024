package com.kemarport.kdmsmahindra.model.setgeofence

data class GetGeofenceByDealer(
    val coordinates: String,
    val createdBy: String,
    val createdDate: String,
    val dealerId: Int,
    val dealerName: Any,
    val displayName: String,
    val isActive: Boolean,
    val locationCode: String,
    val locationId: Int,
    val locationName: String,
    val locationType: String,
    val modifiedBy: Any,
    val modifiedDate: Any,
    val parentLocationCode: Any,
    val parkingCapacity: Int,
    val plantName: Any,
    val remarks: String,
    val runningLoad: Int
)