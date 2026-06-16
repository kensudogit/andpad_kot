package jp.andpad.api.domain

/**
 * 工事案件に紐づく SaaS モジュール別レコードを表すドメインモデル。
 *
 * 黒板、検査、図面、発注、請求など、各 ANDPAD モジュールで登録された
 * 現場記録・業務データの共通表現。案件横断の一覧・検索に使用する。
 *
 * @property id レコードの一意識別子
 * @property projectId 紐づく工事案件 ID
 * @property projectName 紐づく工事案件名
 * @property moduleCode レコードが属する SaaS モジュールコード
 * @property title レコードタイトル・件名
 * @property status レコード状態（モジュール固有のステータス文字列）
 * @property detail 詳細内容・摘要
 * @property amount 関連金額（円）。金額がないレコードの場合は null
 * @property personName 登録者・担当者名
 * @property recordDate レコードの業務日（ISO 8601 日付文字列）
 * @property createdAt システム登録日時（ISO 8601 形式文字列）
 */
data class ProjectModuleRecord(
    val id: String,
    val projectId: String,
    val projectName: String,
    val moduleCode: SaasModuleCode,
    val title: String,
    val status: String,
    val detail: String,
    val amount: Double?,
    val personName: String,
    val recordDate: String,
    val createdAt: String,
)
