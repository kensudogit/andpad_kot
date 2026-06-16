package jp.andpad.api.domain

/**
 * 組織のチームメンバー（ユーザーと組織の所属関係）を表すドメインモデル。
 *
 * 組織への招待・参加、権限ロール、最終アクティブ日時など、
 * メンバー管理画面で表示・操作する情報を保持する。
 *
 * @property id メンバーシップの一意識別子
 * @property user 所属ユーザー情報
 * @property role 組織内での権限ロール
 * @property joinedAt 組織参加日時（ISO 8601 形式文字列）
 * @property lastActiveAt 最終アクティブ日時（ISO 8601 形式文字列）
 */
data class TeamMember(val id: String, val user: User, val role: MemberRole, val joinedAt: String, val lastActiveAt: String)
