/**
 * 認証 REST API コントローラ。
 * ログイン・新規登録・ログアウトを `/auth` 配下で提供し、
 * 成功時に JWT を HttpOnly Cookie（`dv_token`）へ設定する。
 * [AuthService] で資格情報検証、[AppProperties] で Cookie 有効期限を制御する。
 */
package jp.andpad.api.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import jp.andpad.api.auth.dto.AuthResponse
import jp.andpad.api.auth.dto.LoginRequest
import jp.andpad.api.auth.dto.RegisterRequest
import jp.andpad.api.config.AppProperties
import jp.andpad.api.repository.AuthRepository.RegisterInput
import jp.andpad.api.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 認証エンドポイント群。
 *
 * @property authService ログイン・登録のビジネスロジック
 * @property appProperties JWT TTL 等のアプリ設定
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val appProperties: AppProperties,
) {

    /**
     * メールアドレスとパスワードでログインする。
     *
     * @param request ログイン資格情報
     * @param httpRequest Cookie の Secure 判定に使用
     * @param response JWT Cookie を付与するレスポンス
     * @return 成功時は [AuthResponse]（200）、資格情報不一致時は 401
     */
    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<*> {
        return try {
            val payload = authService.login(request.email, request.password)
            setTokenCookie(httpRequest, response, payload.token)
            ResponseEntity.ok(payload)
        } catch (_: IllegalArgumentException) {
            // パスワード不一致等は詳細を漏らさず汎用メッセージを返す
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "invalid credentials"))
        }
    }

    /**
     * 新規クリニック（組織）とオーナーユーザーを登録する。
     *
     * @param request クリニック名・スラッグ・オーナー情報
     * @param httpRequest Cookie の Secure 判定に使用
     * @param response 登録成功時に JWT Cookie を付与
     * @return 成功時は [AuthResponse]（200）、バリデーション等の失敗時は 400
     */
    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<*> {
        return try {
            val payload = authService.register(
                RegisterInput(
                    request.clinicName,
                    request.slug,
                    request.ownerName,
                    request.email,
                    request.password,
                ),
            )
            setTokenCookie(httpRequest, response, payload.token)
            ResponseEntity.ok(payload)
        } catch (ex: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to ex.message))
        }
    }

    /**
     * セッション Cookie を削除してログアウトする。
     *
     * @param request リクエスト（Secure 判定用）
     * @param response 期限切れ Cookie を付与
     * @return `{ "ok": true }`
     */
    @PostMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Map<String, Boolean>> {
        clearTokenCookie(request, response)
        return ResponseEntity.ok(mapOf("ok" to true))
    }

    /**
     * JWT を HttpOnly Cookie としてレスポンスに設定する。
     *
     * @param request プロキシ経由 HTTPS 判定に使用
     * @param response Cookie 付与先
     * @param token 発行済み JWT 文字列
     */
    private fun setTokenCookie(request: HttpServletRequest, response: HttpServletResponse, token: String) {
        val cookie = Cookie("dv_token", token).apply {
            path = "/"
            isHttpOnly = true // XSS からトークンを保護
            secure = cookieSecure(request)
            maxAge = Duration.ofHours(appProperties.jwt.ttlHours.toLong()).seconds.toInt()
        }
        response.addCookie(cookie)
    }

    /**
     * 認証 Cookie を maxAge=0 で上書きし、ブラウザ側で削除させる。
     *
     * @param request Secure フラグ判定用
     * @param response 削除用 Cookie 付与先
     */
    private fun clearTokenCookie(request: HttpServletRequest, response: HttpServletResponse) {
        val cookie = Cookie("dv_token", "").apply {
            path = "/"
            isHttpOnly = true
            secure = cookieSecure(request)
            maxAge = 0
        }
        response.addCookie(cookie)
    }

    /**
     * Cookie の Secure 属性を決定する。
     * 直接 HTTPS 接続、または Railway 等のリバースプロキシ経由（X-Forwarded-Proto）を判定する。
     *
     * @param request 現在の HTTP リクエスト
     * @return HTTPS 相当なら true
     */
    private fun cookieSecure(request: HttpServletRequest): Boolean {
        if (request.isSecure) {
            return true
        }
        // Railway / ロードバランサー背後では X-Forwarded-Proto を参照
        return "https".equals(request.getHeader("X-Forwarded-Proto"), ignoreCase = true)
    }
}
