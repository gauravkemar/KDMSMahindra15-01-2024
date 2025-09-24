package com.kemarport.kdmsmahindra.model.autoupdate

data class GetAppDetailsResponse(
    val errorMessage: Any,
    val exception: Any,
    val responseMessage: Any,
    val responseObject: ResponseObject,
    val statusCode: Int
)
