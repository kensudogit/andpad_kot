package jp.andpad.api.domain

/**
 * 工事案件一覧向けの予算サマリ（簡易版）を表すドメインモデル。
 *
 * 複数案件を横断表示する際に、請負金額・予算・実績・請求・差異率を
 * コンパクトに一覧化するための集約型。
 *
 * @property projectId 工事案件 ID
 * @property projectName 工事案件名
 * @property status 案件のライフサイクルステータス
 * @property contractAmount 請負契約金額（円）
 * @property totalBudget 実行予算合計額（円）
 * @property totalActual 実績原価合計額（円）
 * @property billingTotal 請求済み合計額（円）
 * @property variancePct 予算対実績の差異率（%）
 */
data class ProjectBudgetSummary(
    val projectId: String,
    val projectName: String,
    val status: ConstructionProjectStatus,
    val contractAmount: Double,
    val totalBudget: Double,
    val totalActual: Double,
    val billingTotal: Double,
    val variancePct: Double,
)
