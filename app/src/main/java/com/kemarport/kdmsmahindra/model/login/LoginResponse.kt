package com.kemarport.kdmsmahindra.model.login

data class LoginResponse(
    val coordinates: Any,
    val dealerCode: String,
    val dealerCoordinates: List<DealerCoordinate>,
    val dealerName: String,
    val email: String,
    val firstName: String,
    val id: Int,
    val isPasswordReset: Boolean,
    val isVerified: Boolean,
    val jwtToken: String,
    val lastName: String,
    val locationId: Int,
    val locationsWithoutCoordinates: Int,
    val mobileNumber: String,
    val refreshToken: String,
    val roleName: String,
    val totalLocations: Int,
    val userName: String
)