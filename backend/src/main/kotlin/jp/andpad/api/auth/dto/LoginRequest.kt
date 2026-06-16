/**
 * 認証 API のリクエスト DTO 群。
 * JSON デシリアライズ専用のデータクラスで、
 * [jp.andpad.api.auth.AuthController] から [jp.andpad.api.service.AuthService] へ資格情報を渡す。
 */
package jp.andpad.api.auth.dto

/**
 * ログインリクエストボディ。
 *
 * @property email 登録済みメールアドレス
 * @property password 平文パスワード（サービス層で BCrypt 照合）
 */
data class LoginRequest(val email: String, val password: String)
