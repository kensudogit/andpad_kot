package jp.andpad.api.graphql.input

/**
 * 休暇申請新規作成 Mutation（`createLeaveRequest`）の入力型。
 *
 * 申請者は認証コンテキストのユーザー（未認証時はデモユーザー）として記録される。
 */
data class CreateLeaveRequestInput(
    /** 休暇開始日（ISO 8601 日付文字列）。 */
    val startDate: String,
    /** 休暇終了日（ISO 8601 日付文字列）。 */
    val endDate: String,
    /** 休暇理由・用途。 */
    val reason: String,
)
