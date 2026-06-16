/**
 * 認証成功時のレスポンス DTO。
 * JWT トークンとセッション情報（ユーザー・組織）をクライアントへ返す。
 */
package jp.andpad.api.auth.dto

import jp.andpad.api.domain.Session

/**
 * ログイン・登録成功レスポンス。
 *
 * @property token 発行された JWT（Cookie とレスポンスボディの両方に含まれる）
 * @property session ユーザー ID・組織 ID・ロール等のセッション情報
 */
data class AuthResponse(val token: String, val session: Session)
