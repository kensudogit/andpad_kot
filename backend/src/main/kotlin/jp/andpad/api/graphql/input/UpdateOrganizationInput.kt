package jp.andpad.api.graphql.input

/**
 * 組織プロフィール更新 Mutation（`updateOrganization`）の入力型。
 *
 * GraphQL スキーマ上の `UpdateOrganizationInput` に対応し、
 * 指定されたフィールドのみ組織レコードへパッチ適用される。
 */
data class UpdateOrganizationInput(
    /** 組織の表示名称。 */
    val name: String,
    /** URL 等で利用する組織スラッグ（一意識別子）。 */
    val slug: String,
    /** 契約席数（ライセンス数）。`null` の場合は席数を変更しない。 */
    val seatCount: Int?,
    /** IANA タイムゾーン ID（例: `Asia/Tokyo`）。 */
    val timezone: String,
)
