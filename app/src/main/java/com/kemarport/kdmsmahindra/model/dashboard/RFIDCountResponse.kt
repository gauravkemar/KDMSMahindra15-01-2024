package com.kemarport.kdmsmahindra.model.dashboard

data class RFIDCountResponse(
    val dealerCode: String,
    val maxScans: Int,
    val rfid: Int,
    val scans: Int
)