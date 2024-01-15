package com.kemarport.kdmsmahindra.model.newapi

data class VerifyDealerVehicleResponse(
    val colorDescription: String,
    val engineNo: String,
    val message: String,
    val modelCode: String,
    val status: String,
    val vin: String
)