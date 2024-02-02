package com.kemarport.kdmsmahindra.model.forgotpassword

data class ResetPasswordRequest(
    val confirmPassword: String,
    val password: String,
    val token: String
)