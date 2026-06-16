/**
 * マルチテナント用セキュリティコンテキストヘルパ。
 * Spring Security の SecurityContext から [AuthPrincipal] を取得し、
 * 組織 ID・ユーザー ID をリポジトリ層のテナントフィルタに供給する。
 * 未認証時はデモ組織（org_demo）へフォールバックする。
 */
package jp.andpad.api.security

import java.util.Optional
import org.springframework.security.core.context.SecurityContextHolder

/**
 * 現在リクエストの認証プリンシパルとテナント ID を解決するユーティリティ。
 *
 * - [DEMO_ORG_ID] / [DEMO_USER_ID]: 未ログイン時のデモテナント識別子
 */
object TenantContext {
    /** デモ組織 ID（シードデータ org_demo） */
    const val DEMO_ORG_ID: String = "org_demo"
    /** デモユーザー ID（シードデータ user_demo） */
    const val DEMO_USER_ID: String = "user_demo"

    /**
     * SecurityContext から [AuthPrincipal] を取得する。
     *
     * @return 認証済みなら Optional.of(principal)、未認証または別種 principal なら empty
     */
    fun principal(): Optional<AuthPrincipal> {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.principal is AuthPrincipal) {
            return Optional.of(auth.principal as AuthPrincipal)
        }
        return Optional.empty()
    }

    /**
     * 現在の組織 ID を返す。未認証時は [DEMO_ORG_ID]。
     *
     * @return 組織 ID 文字列
     */
    fun orgId(): String = principal().map { it.orgId }.orElse(DEMO_ORG_ID)

    /**
     * 現在のユーザー ID を返す。未認証時は [DEMO_USER_ID]。
     *
     * @return ユーザー ID 文字列
     */
    fun userId(): String = principal().map { it.userId }.orElse(DEMO_USER_ID)

    /**
     * 認証必須の操作でプリンシパルを取得する。
     *
     * @return 認証済み [AuthPrincipal]
     * @throws UnauthorizedException 未認証時
     */
    fun requirePrincipal(): AuthPrincipal =
        principal().orElseThrow { UnauthorizedException("authentication required") }
}
