package jp.andpad.api.domain

/**
 * 工事案件の原価実績エントリ（原価計上明細）を表すドメインモデル。
 *
 * 材料費・労務費・外注費など、現場で発生した原価を明細行に紐づけて記録する。
 *
 * @property id 原価エントリの一意識別子
 * @property projectId 紐づく工事案件 ID
 * @property projectName 紐づく工事案件名
 * @property lineItemId 紐づく予算明細行 ID
 * @property lineItemName 紐づく予算明細行名
 * @property entryType 原価種別（材料・労務・外注など）
 * @property vendorName 取引先・業者名
 * @property description 原価の摘要・説明
 * @property amount 原価金額（円）
 * @property entryDate 計上日（ISO 8601 日付文字列）
 * @property invoiceNo 請求書・伝票番号
 * @property recordedBy 原価を記録したユーザー名
 * @property createdAt レコード作成日時（ISO 8601 形式文字列）
 */
data class CostEntry(
    val id: String,
    val projectId: String,
    val projectName: String,
    val lineItemId: String,
    val lineItemName: String,
    val entryType: CostEntryType,
    val vendorName: String,
    val description: String,
    val amount: Double,
    val entryDate: String,
    val invoiceNo: String,
    val recordedBy: String,
    val createdAt: String,
)
