package jp.andpad.api.domain

/**
 * 予算の明細行（費目・WBS 単位）を表すドメインモデル。
 *
 * 工種別・費目別の見積・予算・実績・差異を管理し、原価管理ダッシュボードの集計単位となる。
 *
 * @property id 明細行の一意識別子
 * @property budgetId 紐づく予算ヘッダ ID
 * @property categoryCode 費目コード（例: 「01-010」）
 * @property categoryName 費目名（例: 「直接工事費・仮設」）
 * @property wbsCode WBS（Work Breakdown Structure）コード
 * @property description 明細の説明・摘要
 * @property estimateAmount 見積金額（円）
 * @property budgetAmount 実行予算金額（円）
 * @property committedAmount 発注・コミット済み金額（円）
 * @property actualAmount 実績原価金額（円）
 * @property varianceAmount 予算対実績の差異金額（円）
 * @property variancePct 予算対実績の差異率（%）
 * @property sortOrder 表示順序
 * @property createdAt 明細作成日時（ISO 8601 形式文字列）
 */
data class BudgetLineItem(
    val id: String,
    val budgetId: String,
    val categoryCode: String,
    val categoryName: String,
    val wbsCode: String,
    val description: String,
    val estimateAmount: Double,
    val budgetAmount: Double,
    val committedAmount: Double,
    val actualAmount: Double,
    val varianceAmount: Double,
    val variancePct: Double,
    val sortOrder: Int,
    val createdAt: String,
)
