package com.indusjs.fleet.core.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Lightweight JWT payload decoder for client-side claim inspection.
 * Uses only the Base64-decoded payload — no signature verification.
 */
object JwtHelper {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Decodes the JWT payload and returns the claims as a [JsonObject],
     * or `null` if the token is malformed.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun decodeClaims(jwt: String): JsonObject? {
        val parts = jwt.split(".")
        if (parts.size != 3) return null
        return try {
            val payloadBytes = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                .decode(parts[1])
            json.decodeFromString<JsonObject>(payloadBytes.decodeToString())
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Returns the `tid` (tenant_id) claim from a JWT, or `null` if absent / malformed.
     */
    fun extractTenantId(jwt: String): String? {
        val claims = decodeClaims(jwt) ?: return null
        return claims["tid"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
    }

    /** `true` when the JWT contains a non-blank `tid` claim. */
    fun hasTenantContext(jwt: String): Boolean = extractTenantId(jwt) != null
}
