package com.kemarport.kdmsmahindra.model.login

data class LoginRequest(
    val DeviceId: String,
    val Password: String,
    val UserName: String
)