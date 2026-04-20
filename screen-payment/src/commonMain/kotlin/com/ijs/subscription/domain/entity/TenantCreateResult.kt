package com.ijs.subscription.domain.entity

data class TenantCreateResult(
    val tenantId: String,
    val accessToken: String,
    val refreshToken: String
)
