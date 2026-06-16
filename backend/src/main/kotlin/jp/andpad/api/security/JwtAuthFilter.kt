/**
 * JWT 認証 Servlet フィルタ。
 * 各 HTTP リクエストで Authorization ヘッダまたは `dv_token` Cookie から
 * JWT を抽出し、Spring Security の SecurityContext に [AuthPrincipal] を設定する。
 * [JwtService] による署名検証に依存する。
 */
package jp.andpad.api.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * リクエストごとに JWT を検証する OncePerRequestFilter。
 *
 * @property jwtService トークン発行・検証サービス
 */
@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    /**
     * トークンを抽出・検証し、SecurityContext を更新してから後続フィルタへ委譲する。
     * トークンが無効な場合はコンテキストをクリアするが、リクエスト自体はブロックしない
     * （エンドポイント側で [TenantContext.requirePrincipal] 等により認可する）。
     *
     * @param request HTTP リクエスト
     * @param response HTTP レスポンス
     * @param filterChain 後続フィルタチェーン
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Bearer ヘッダを優先し、無ければ Cookie から取得（SPA + Cookie 認証両対応）
        var token = extractBearer(request)
        if (token == null) {
            token = extractCookie(request, "dv_token")
        }
        if (token != null) {
            try {
                val principal = jwtService.parseToken(token)
                val authentication = UsernamePasswordAuthenticationToken(principal, null, emptyList())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (_: Exception) {
                // 期限切れ・改ざんトークンは匿名扱いにフォールバック
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }

    /**
     * `Authorization: Bearer <token>` ヘッダから JWT を取り出す。
     *
     * @param request HTTP リクエスト
     * @return トークン文字列、ヘッダが無い場合は null
     */
    private fun extractBearer(request: HttpServletRequest): String? {
        val auth = request.getHeader("Authorization")
        if (auth != null && auth.startsWith("Bearer ", ignoreCase = true)) {
            return auth.substring(7).trim()
        }
        return null
    }

    /**
     * 指定名の Cookie から非空トークン値を取得する。
     *
     * @param request HTTP リクエスト
     * @param name Cookie 名（例: `dv_token`）
     * @return Cookie 値、見つからないか空の場合は null
     */
    private fun extractCookie(request: HttpServletRequest, name: String): String? {
        val cookies = request.cookies ?: return null
        for (cookie in cookies) {
            if (name == cookie.name && cookie.value != null && !cookie.value.isBlank()) {
                return cookie.value
            }
        }
        return null
    }
}
