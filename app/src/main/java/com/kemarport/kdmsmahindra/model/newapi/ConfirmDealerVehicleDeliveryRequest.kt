package com.kemarport.kdmsmahindra.model.newapi

data class ConfirmDealerVehicleDeliveryRequest(
    val Coordinates: String,
    val DealerCode: String,
    val MovementType: String,
    val UserName: String,
    val VIN: String,
    val RFIDTag: String,
    val LocationId: Int


)