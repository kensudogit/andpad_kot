package jp.andpad.api.graphql.input

/**
 * 契約書インスタンス新規作成 Mutation（`createContract`）の入力型。
 *
 * 既存テンプレートをベースに、相手方情報を埋め込んだ契約書を生成する。
 */
data class CreateContractInput(
    /** 元となる契約書テンプレート ID。 */
    val templateId: String,
    /** 契約書タイトル（件名）。 */
    val title: String,
    /** 契約相手方の名称（法人名・個人名）。 */
    val partyName: String,
    /** 契約相手方のメールアドレス（署名依頼送付先等）。 */
    val partyEmail: String,
    /** 契約書本文。テンプレート内容を上書き・追記する場合に使用。 */
    val body: String,
)
