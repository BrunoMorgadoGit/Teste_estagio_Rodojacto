package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.entity.Collaborator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val signingKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    fun generateToken(collaborator: Collaborator): String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.expirationMs)
        val collaboratorId = collaborator.id ?: error("Collaborator id should not be null")
        val organizationId = collaborator.organization.id ?: error("Organization id should not be null")

        return Jwts.builder()
            .subject(collaborator.email)
            .claim("collaboratorId", collaboratorId)
            .claim("email", collaborator.email)
            .claim("accessLevel", collaborator.accessLevel.name)
            .claim("organizationId", organizationId)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }

    fun extractUsername(token: String): String =
        extractAllClaims(token).subject

    fun validateToken(token: String): Boolean =
        runCatching {
            extractAllClaims(token)
            true
        }.getOrDefault(false)

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
