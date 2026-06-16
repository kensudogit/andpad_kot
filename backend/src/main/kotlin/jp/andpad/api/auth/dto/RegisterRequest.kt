/**
 * 新規登録 API のリクエスト DTO。
 * 組織（クリニック）作成とオーナーユーザー登録に必要な入力を保持する。
 */
package jp.andpad.api.auth.dto

/**
 * 新規登録リクエストボディ。
 *
 * @property clinicName 組織（クリニック）の表示名
 * @property slug URL 用スラッグ（一意識別子）
 * @property ownerName オーナーユーザーの氏名
 * @property email オーナーのログインメールアドレス
 * @property password 平文パスワード（保存前に BCrypt ハッシュ化）
 */
data class RegisterRequest(val clinicName: String, val slug: String, val ownerName: String, val email: String, val password: String)
