/**
 * アプリケーション共通の設定プロパティ。
 * application.yml の `app.*` プレフィックスを Kotlin データクラスにバインドし、
 * JWT 署名・TTL 等の実行時パラメータを [JwtService] や [jp.andpad.api.auth.AuthController] へ供給する。
 */
package jp.andpad.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * `app` プレフィックスのルート設定。
 *
 * @property jwt JWT 発行に関する設定
 */
@ConfigurationProperties(prefix = "app")
data class AppProperties(val jwt: Jwt) {
    /**
     * JWT 署名と有効期限の設定。
     *
     * @property secret HMAC-SHA256 署名用シークレット
     * @property ttlHours トークン有効時間（時間単位）
     */
    data class Jwt(val secret: String, val ttlHours: Int)
}
