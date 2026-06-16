package jp.andpad.api.domain

/**
 * 工事案件の予算（実行予算・見積・フォーキャスト）を表すドメインモデル。
 *
 * 請負金額、見積合計、実行予算、コミット済み金額、実績原価など、
 * 原価管理の基盤となる予算ヘッダ情報を保持する。
 *
 * @property id 予算の一意識別子
 * @property projectId 紐づく工事案件 ID
 * @property projectName 紐づく工事案件名
 * @property name 予算名称（例: 「第1版実行予算」）
 * @property budgetType 予算種別（見積・実行予算・フォーキャスト）
 * @property status 予算の承認・ロック状態
 * @property versionNo 予算バージョン番号
 * @property contractAmount 請負契約金額（円）
 * @property totalEstimate 見積合計額（円）
 * @property totalBudget 実行予算合計額（円）
 * @property totalCommitted 発注・コミット済み合計額（円）
 * @property totalActual 実績原価合計額（円）
 * @property notes 備考・注記
 * @property approvedAt 承認日時（ISO 8601 形式文字列）。未承認の場合は空文字
 * @property createdAt 予算作成日時（ISO 8601 形式文字列）
 */
data class ProjectBudget(
    val id: String,
    val projectId: String,
    val projectName: String,
    val name: String,
    val budgetType: BudgetType,
    val status: BudgetStatus,
    val versionNo: Int,
    val contractAmount: Double,
    val totalEstimate: Double,
    val totalBudget: Double,
    val totalCommitted: Double,
    val totalActual: Double,
    val notes: String,
    val approvedAt: String,
    val createdAt: String,
)
