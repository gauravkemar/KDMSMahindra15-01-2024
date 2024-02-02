package com.kemarport.kdmsmahindra.model.forgotpassword

data class ForgotPasswordResponse(
    val emailId: String,
    val errorMessage: String,
    val forgetPasswordOTP: String,
    val id: Int,
    val resetToken: String,
    val resetTokenExpires: String
)