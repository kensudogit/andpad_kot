package jp.andpad.api.domain

/**
 * 認証済みユーザーのセッション情報を表すドメインモデル。
 *
 * ログイン後の API リクエストで、現在のユーザー・所属組織・権限ロールを
 * 一括して参照するためのコンテキストオブジェクト。
 *
 * @property user ログイン中のユーザー
 * @property organization ユーザーが所属する組織（テナント）
 * @property role 組織内でのメンバー権限ロール
 */
data class Session(val user: User, val organization: Organization, val role: MemberRole)
