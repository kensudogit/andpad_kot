package jp.andpad.api.domain

/**
 * DX（デジタルトランスフォーメーション）推進施策を表すドメインモデル。
 *
 * 紙ベース業務のデジタル化、IoT 導入、BIM 活用など、
 * 組織内の DX プロジェクトの進捗とタスク完了状況を管理する。
 *
 * @property id 施策の一意識別子
 * @property title 施策タイトル
 * @property description 施策の概要説明
 * @property status 施策状態（例: 「planning」「in_progress」「completed」）
 * @property progressPct 進捗率（%）
 * @property ownerName 施策オーナー（責任者）名
 * @property dueDate 完了目標日（ISO 8601 日付文字列）
 * @property taskCount 配下タスクの総数
 * @property tasksDone 完了済みタスク数
 * @property createdAt 施策登録日時（ISO 8601 形式文字列）
 */
data class DxInitiative(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val progressPct: Int,
    val ownerName: String,
    val dueDate: String,
    val taskCount: Int,
    val tasksDone: Int,
    val createdAt: String,
)
