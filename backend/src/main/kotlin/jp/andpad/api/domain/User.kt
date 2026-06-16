package jp.andpad.api.domain

/**
 * ANDPAD 建設 PM プラットフォームのユーザー（利用者）を表すドメインモデル。
 *
 * 元請・下請・監理者など、組織に所属して工事案件を管理する個人アカウントの基本情報を保持する。
 *
 * @property id ユーザーの一意識別子
 * @property email ログイン用メールアドレス
 * @property name 表示名（氏名）
 * @property avatarUrl プロフィール画像 URL
 */
data class User(val id: String, val email: String, val name: String, val avatarUrl: String)
