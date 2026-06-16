/**
 * 認証・認可失敗を表すランタイム例外。
 * [TenantContext.requirePrincipal] 等で未認証アクセスを検出した際にスローし、
 * GraphQL / REST の例外ハンドラでクライアントへ返す。
 */
package jp.andpad.api.security

/**
 * 認証が必要な操作でプリンシパルが無い場合の例外。
 *
 * @param message クライアント向けまたはログ用メッセージ
 */
class UnauthorizedException(message: String) : RuntimeException(message)
