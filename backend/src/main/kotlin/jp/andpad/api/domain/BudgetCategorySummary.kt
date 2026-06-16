package jp.andpad.api.domain

/**
 * 費目別の予算・実績サマリを表すドメインモデル。
 *
 * 原価管理ダッシュボードで費目コード単位に集計した予算額、実績額、差異を表示する。
 *
 * @property categoryCode 費目コード
 * @property categoryName 費目名
 * @property budgetAmount 費目の予算合計額（円）
 * @property actualAmount 費目の実績原価合計額（円）
 * @property varianceAmount 予算対実績の差異金額（円）
 */
data class BudgetCategorySummary(
    val categoryCode: String,
    val categoryName: String,
    val budgetAmount: Double,
    val actualAmount: Double,
    val varianceAmount: Double,
)
