package jp.andpad.api.graphql.input

import jp.andpad.api.domain.SaasModuleCode

/**
 * プロジェクト業務レコード新規作成 Mutation（`createProjectModuleRecord`）の入力型。
 *
 * 請求・安全・品質等、SaaS モジュール別の現場記録をプロジェクトに紐付けて登録する。
 */
data class CreateProjectModuleRecordInput(
    /** 紐付け先の建設プロジェクト ID。 */
    val projectId: String,
    /** 記録の属する SaaS モジュール種別。 */
    val moduleCode: SaasModuleCode,
    /** レコードのタイトル（件名）。 */
    val title: String,
    /** 業務ステータス（例: open, done）。 */
    val status: String,
    /** 詳細説明・備考。 */
    val detail: String,
    /** 関連金額（請求額等）。未設定の場合は `null`。 */
    val amount: Double?,
    /** 担当者名。 */
    val personName: String,
    /** 記録日（ISO 8601 日付文字列）。 */
    val recordDate: String,
)
