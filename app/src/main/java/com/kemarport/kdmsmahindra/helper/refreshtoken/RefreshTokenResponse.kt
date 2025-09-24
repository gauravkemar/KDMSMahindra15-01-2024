package com.kemarport.kdmsmahindra.helper.refreshtoken

data class RefreshTokenResponse(
    val jwtToken: String,
    val refreshToken: String
)
