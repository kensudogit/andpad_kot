package jp.andpad.api.domain

/**
 * 請求と原価の突合（リコンサイル）明細 1 件を表すドメインモデル。
 *
 * 出来高請求・中間請求と対応する原価実績を照合し、請求残・原価差異を管理する。
 *
 * @property billingRecordId 請求レコード ID
 * @property title 請求件名・摘要
 * @property billingAmount 請求金額（円）
 * @property costAmount 対応する原価金額（円）
 * @property varianceAmount 請求対原価の差異金額（円）
 * @property status 突合状態（例: 「matched」「pending」「discrepancy」）
 * @property billingDate 請求日（ISO 8601 日付文字列）
 */
data class BillingReconciliationItem(
    val billingRecordId: String,
    val title: String,
    val billingAmount: Double,
    val costAmount: Double,
    val varianceAmount: Double,
    val status: String,
    val billingDate: String,
)
