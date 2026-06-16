package jp.andpad.api.graphql.input

import jp.andpad.api.domain.BudgetStatus
import jp.andpad.api.domain.BudgetType

/**
 * プロジェクト予算新規作成 Mutation（`createProjectBudget`）の入力型。
 *
 * 実行予算・見積・変更予算等の予算ヘッダ情報を登録する。
 */
data class CreateProjectBudgetInput(
    /** 紐付け先の建設プロジェクト ID。 */
    val projectId: String,
    /** 予算名称（例: 第1版実行予算）。 */
    val name: String,
    /** 予算種別（見積・実行・変更等）。 */
    val budgetType: BudgetType,
    /** 予算ステータス（下書き・承認待ち等）。 */
    val status: BudgetStatus,
    /** 版番号。初版作成時は `null` 可。 */
    val versionNo: Int?,
    /** 請負契約金額。未設定の場合は `null`。 */
    val contractAmount: Double?,
    /** 予算に関する備考・注記。 */
    val notes: String,
)
