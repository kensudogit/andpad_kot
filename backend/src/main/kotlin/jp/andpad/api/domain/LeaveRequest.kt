package jp.andpad.api.domain

/**
 * 休暇・欠勤申請を表すドメインモデル。
 *
 * 有給休暇、特別休暇、欠勤などの申請と承認ワークフローを管理する。
 *
 * @property id 休暇申請の一意識別子
 * @property userId 申請者ユーザー ID
 * @property userName 申請者名
 * @property startDate 休暇開始日（ISO 8601 日付文字列）
 * @property endDate 休暇終了日（ISO 8601 日付文字列）
 * @property reason 申請理由
 * @property status 申請状態（例: 「pending」「approved」「rejected」）
 * @property createdAt 申請作成日時（ISO 8601 形式文字列）
 */
data class LeaveRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String,
    val createdAt: String,
)
