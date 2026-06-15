package jp.andpad.api.security

import io.jsonwebtoken.Jwts
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import jp.andpad.api.config.AppProperties
import org.springframework.stereotype.Service

@Service
class JwtService(private val appProperties: AppProperties) {

    fun issueToken(principal: AuthPrincipal): String {
        val now = Instant.now()
        val expires = now.plus(appProperties.jwt.ttlHours.toLong(), ChronoUnit.HOURS)
        return Jwts.builder()
            .claim("uid", principal.userId)
            .claim("oid", principal.orgId)
            .claim("role", principal.role)
            .claim("email", principal.email)
            .claim("name", principal.name)
            .subject(principal.userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expires))
            .signWith(signingKey())
            .compact()
    }

    fun parseToken(token: String): AuthPrincipal {
        val claims = Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .payload
        return AuthPrincipal(
            claims.get("uid", String::class.java),
            claims.get("oid", String::class.java),
            claims.get("role", String::class.java),
            claims.get("email", String::class.java),
            claims.get("name", String::class.java),
        )
    }

    private fun signingKey() =
        SecretKeySpec(appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
}
