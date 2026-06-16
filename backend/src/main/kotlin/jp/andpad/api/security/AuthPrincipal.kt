/**
 * JWT クレームから復元する認証プリンシパル。
 * Spring Security の Authentication.principal として保持され、
 * [TenantContext] や GraphQL リゾルバからテナント・ユーザー情報を参照する。
 */
package jp.andpad.api.security

/**
 * 認証済みユーザーの識別情報。
 *
 * @property userId ユーザー一意 ID（JWT subject / uid クレーム）
 * @property orgId 所属組織 ID（マルチテナント分離キー）
 * @property role 組織内ロール（例: OWNER, MEMBER）
 * @property email ログインメールアドレス
 * @property name 表示名
 */
data class AuthPrincipal(
    val userId: String,
    val orgId: String,
    val role: String,
    val email: String,
    val name: String,
)
