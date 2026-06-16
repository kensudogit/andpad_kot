package jp.andpad.api.graphql.input

/**
 * 予算費目（明細行）新規作成 Mutation（`createBudgetLineItem`）の入力型。
 *
 * WBS・カテゴリ単位の予算明細を親予算に追加する。
 */
data class CreateBudgetLineItemInput(
    /** 親となるプロジェクト予算 ID。 */
    val budgetId: String,
    /** 費目カテゴリコード（会計・原価コード）。 */
    val categoryCode: String,
    /** 費目カテゴリの表示名。 */
    val categoryName: String,
    /** WBS（Work Breakdown Structure）コード。 */
    val wbsCode: String,
    /** 費目の説明・摘要。 */
    val description: String,
    /** 見積金額。未設定の場合は `null`。 */
    val estimateAmount: Double?,
    /** 予算金額（実行予算額）。未設定の場合は `null`。 */
    val budgetAmount: Double?,
    /** 発注済み（コミット）金額。未設定の場合は `null`。 */
    val committedAmount: Double?,
    /** 一覧表示時の並び順。未指定の場合は `null`（デフォルト順）。 */
    val sortOrder: Int?,
)
