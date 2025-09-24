package com.kemarport.kdmsmahindra.model.autoupdate

data class ResponseObject(
    val apkFileUrl: String,
    val apkVersion: Int,
    val apkVersionDisplayString: String,
    val isMandatory: Boolean
)