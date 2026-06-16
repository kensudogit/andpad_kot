package jp.andpad.api.graphql.input

import jp.andpad.api.domain.CostEntryType

/**
 * 原価エントリ（実績コスト）手動登録 Mutation（`createCostEntry`）の入力型。
 *
 * 請求書・支払等に基づく原価実績を費目に紐付けて記録する。
 */
data class CreateCostEntryInput(
    /** 紐付け先の建設プロジェクト ID。 */
    val projectId: String,
    /** 紐付け先の予算費目（明細行）ID。 */
    val lineItemId: String,
    /** 原価種別（材料費・外注費・間接費等）。 */
    val entryType: CostEntryType,
    /** 取引先・業者名。 */
    val vendorName: String,
    /** 原価内容の説明。 */
    val description: String,
    /** 原価金額（税抜または税込は運用に依存）。 */
    val amount: Double,
    /** 計上日（ISO 8601 日付文字列）。 */
    val entryDate: String,
    /** 請求書番号。未発行の場合は空文字可。 */
    val invoiceNo: String,
    /** 記録者（入力担当者）名。 */
    val recordedBy: String,
)
