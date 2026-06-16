package jp.andpad.api.graphql.input

import jp.andpad.api.domain.ConstructionProjectStatus

/**
 * 建設プロジェクト新規作成 Mutation（`createConstructionProject`）の入力型。
 *
 * GraphQL スキーマ上の `CreateConstructionProjectInput` に対応する。
 */
data class CreateConstructionProjectInput(
    /** 工事案件名。 */
    val name: String,
    /** 現場住所（所在地）。 */
    val siteAddress: String,
    /** 案件ステータス（計画中・進行中・完了等）。 */
    val status: ConstructionProjectStatus,
    /** 現場責任者・プロジェクトマネージャー名。 */
    val managerName: String,
    /** 工期開始日（ISO 8601 日付文字列）。 */
    val startDate: String,
    /** 工期終了日（ISO 8601 日付文字列）。 */
    val endDate: String,
)
