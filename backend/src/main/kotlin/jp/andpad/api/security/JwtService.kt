/**
 * JWT 発行・検証サービス。
 * HMAC-SHA256 で署名したアクセストークンを生成し、
 * ユーザー ID・組織 ID・ロール等のクレームを [AuthPrincipal] として復元する。
 * シークレットと TTL は [jp.andpad.api.config.AppProperties] から取得する。
 */
package jp.andpad.api.security

import io.jsonwebtoken.Jwts
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.spec.SecretKeySpec
import jp.andpad.api.config.AppProperties
import org.springframework.stereotype.Service

/**
 * JWT の生成とパースを担う Spring サービス。
 *
 * @property appProperties JWT シークレット・有効時間設定
 */
@Service
class JwtService(private val appProperties: AppProperties) {

    /**
     * 認証済みプリンシパルから JWT を発行する。
     *
     * @param principal ユーザー・組織・ロール情報
     * @return 署名済み JWT 文字列
     */
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

    /**
     * JWT を検証し [AuthPrincipal] にデコードする。
     *
     * @param token Bearer または Cookie から取得した JWT
     * @return クレームから復元したプリンシパル
     * @throws io.jsonwebtoken.JwtException 署名不一致・期限切れ・形式不正時
     */
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

    /**
     * 設定値から HMAC-SHA256 署名鍵を生成する。
     *
     * @return JWT 署名用 SecretKeySpec
     */
    private fun signingKey() =
        SecretKeySpec(appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
}
