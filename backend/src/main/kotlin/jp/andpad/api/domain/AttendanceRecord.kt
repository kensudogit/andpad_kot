package jp.andpad.api.domain

/**
 * 勤怠打刻レコードを表すドメインモデル。
 *
 * 現場作業員・事務員の出退勤時刻を記録し、労務管理・安全配慮義務の
 * 履行状況を把握するためのエンティティ。
 *
 * @property id 勤怠レコードの一意識別子
 * @property userId 対象ユーザー ID
 * @property userName 対象ユーザー名
 * @property clockIn 出勤打刻日時（ISO 8601 形式文字列）
 * @property clockOut 退勤打刻日時（ISO 8601 形式文字列）。未退勤の場合は空文字
 * @property note 備考（遅刻・早退理由、現場移動など）
 */
data class AttendanceRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val clockIn: String,
    val clockOut: String,
    val note: String,
)
