package jp.andpad.api.domain

/**
 * 工事案件の原価管理ダッシュボードを表すドメインモデル。
 *
 * 請負金額、予算・実績・フォーキャスト、粗利率、請求状況、月次原価推移、
 * 明細行、最近の原価エントリなど、案件の収支を俯瞰する集約ビュー。
 *
 * @property projectId 対象工事案件 ID
 * @property projectName 対象工事案件名
 * @property contractAmount 請負契約金額（円）
 * @property totalEstimate 見積合計額（円）
 * @property totalBudget 実行予算合計額（円）
 * @property totalCommitted 発注・コミット済み合計額（円）
 * @property totalActual 実績原価合計額（円）
 * @property totalForecast フォーキャスト（完成時予想原価）合計額（円）
 * @property varianceAmount 予算対実績の差異金額（円）
 * @property variancePct 予算対実績の差異率（%）
 * @property completionPct 工事進捗率（%）
 * @property estimateBudgetTotal 見積対予算の合計比較額（円）
 * @property grossMarginPct 粗利率（%）
 * @property inquiryProfitTotal 問合せ利益（見込み利益）合計額（円）
 * @property billingTotal 請求済み合計額（円）
 * @property billingBalance 請求残高（未請求・未入金残）（円）
 * @property monthlyCosts 月次原価推移
 * @property reconciliation 請求と原価の突合（リコンサイル）明細
 * @property lineItems 予算明細行一覧
 * @property recentCosts 最近登録された原価エントリ一覧
 * @property categorySummary 費目別サマリ一覧
 * @property generatedAt ダッシュボード生成日時（ISO 8601 形式文字列）
 */
data class BudgetDashboard(
    val projectId: String,
    val projectName: String,
    val contractAmount: Double,
    val totalEstimate: Double,
    val totalBudget: Double,
    val totalCommitted: Double,
    val totalActual: Double,
    val totalForecast: Double,
    val varianceAmount: Double,
    val variancePct: Double,
    val completionPct: Double,
    val estimateBudgetTotal: Double,
    val grossMarginPct: Double,
    val inquiryProfitTotal: Double,
    val billingTotal: Double,
    val billingBalance: Double,
    val monthlyCosts: List<MonthlyCostMetric>,
    val reconciliation: List<BillingReconciliationItem>,
    val lineItems: List<BudgetLineItem>,
    val recentCosts: List<CostEntry>,
    val categorySummary: List<BudgetCategorySummary>,
    val generatedAt: String,
)
