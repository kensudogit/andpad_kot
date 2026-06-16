/**
 * Spring Security 設定。
 * ステートレス JWT 認証、CORS、パスワードエンコーダを構成し、
 * ヘルスチェック・認証・GraphQL エンドポイントを公開する。
 * [JwtAuthFilter] を UsernamePasswordAuthenticationFilter より前に挿入する。
 */
package jp.andpad.api.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * HTTP セキュリティと CORS の Bean 定義。
 *
 * @property jwtAuthFilter 各リクエストで JWT を SecurityContext に載せるフィルタ
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    /**
     * セキュリティフィルタチェーンを構築する。
     * CSRF は API 専用のため無効化し、セッションは STATELESS（JWT ベース）とする。
     *
     * @param http HttpSecurity ビルダー
     * @return 構成済み [SecurityFilterChain]
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/health", "/status", "/auth/**", "/graphiql", "/graphql")
                    .permitAll()
                    .anyRequest()
                    .permitAll() // GraphQL リゾルバ側で TenantContext により認可
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    /**
     * ユーザー登録・ログイン時のパスワードハッシュ化に BCrypt を使用する。
     *
     * @return BCrypt 実装の [PasswordEncoder]
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * フロントエンド（localhost:3000）からの Cookie 付き CORS を許可する。
     *
     * @return すべての URL パスに適用する CORS 設定
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("http://localhost:3000")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true // HttpOnly Cookie 認証に必須
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
