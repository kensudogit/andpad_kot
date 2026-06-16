package jp.andpad.api.graphql.input

/**
 * DX 推進イニシアチブ新規作成 Mutation（`createDxInitiative`）の入力型。
 *
 * 組織のデジタル化・業務改善施策を登録する。
 */
data class CreateDxInitiativeInput(
    /** 施策タイトル。 */
    val title: String,
    /** 施策の目的・内容の詳細説明。 */
    val description: String,
    /** 進行ステータス（例: planned, in_progress, done）。 */
    val status: String,
    /** 進捗率（0〜100）。未設定の場合は `null`。 */
    val progressPct: Int?,
    /** 施策オーナー（担当責任者）名。 */
    val ownerName: String,
    /** 完了目標日（ISO 8601 日付文字列）。 */
    val dueDate: String,
)
