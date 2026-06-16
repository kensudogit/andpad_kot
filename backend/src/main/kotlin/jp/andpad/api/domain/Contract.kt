package jp.andpad.api.domain

/**
 * 電子契約（e 契約）の契約書インスタンスを表すドメインモデル。
 *
 * 下請契約書、覚書、安全協力誓約書など、テンプレートから生成された
 * 個別契約書の内容・署名状態を管理する。
 *
 * @property id 契約書の一意識別子
 * @property templateId 元となった契約テンプレート ID
 * @property title 契約書タイトル
 * @property partyName 契約相手方（署名者）の名称
 * @property partyEmail 契約相手方のメールアドレス
 * @property body 契約書本文（HTML またはプレーンテキスト）
 * @property status 契約状態（例: 「draft」「sent」「signed」「expired」）
 * @property createdAt 契約書作成日時（ISO 8601 形式文字列）
 * @property signedAt 署名完了日時（ISO 8601 形式文字列）。未署名の場合は空文字
 */
data class Contract(
    val id: String,
    val templateId: String,
    val title: String,
    val partyName: String,
    val partyEmail: String,
    val body: String,
    val status: String,
    val createdAt: String,
    val signedAt: String,
)
