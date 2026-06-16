package jp.andpad.api.graphql.input

/**
 * CRM コンタクト新規作成 Mutation（`createCrmContact`）の入力型。
 *
 * 取引先・見込み客の連絡先情報を CRM に登録する。
 */
data class CreateCrmContactInput(
    /** コンタクト氏名。 */
    val name: String,
    /** メールアドレス。 */
    val email: String,
    /** 電話番号。 */
    val phone: String,
    /** 所属会社名。 */
    val company: String,
    /** 商談ステージ（例: lead, qualified, customer）。 */
    val stage: String,
    /** 自由記述のメモ・備考。 */
    val notes: String,
)
