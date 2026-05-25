package com.bruno.projeto_estagio_jacto.security

import com.bruno.projeto_estagio_jacto.entity.AccessLevel
import com.bruno.projeto_estagio_jacto.entity.Collaborator
import com.bruno.projeto_estagio_jacto.entity.Organization
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class JwtTokenProviderTest {

    @Test
    fun `should include required collaborator claims`() {
        val secret = "test-secret-key-with-enough-length-for-hmac"
        val tokenProvider = JwtTokenProvider(JwtProperties(secret = secret, expirationMs = 60_000))
        val organization = Organization(
            id = 10,
            corporateName = "Organization A",
            registrationCode = "ORG-A",
        )
        val collaborator = Collaborator(
            id = 20,
            fullName = "Manager",
            email = "manager@jacto.com",
            password = "encoded-password",
            accessLevel = AccessLevel.MANAGER,
            organization = organization,
        )

        val claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(tokenProvider.generateToken(collaborator))
            .payload

        assertThat(claims.subject).isEqualTo("manager@jacto.com")
        assertThat((claims["collaboratorId"] as Number).toLong()).isEqualTo(20)
        assertThat(claims["email"]).isEqualTo("manager@jacto.com")
        assertThat(claims["accessLevel"]).isEqualTo("MANAGER")
        assertThat((claims["organizationId"] as Number).toLong()).isEqualTo(10)
    }
}
