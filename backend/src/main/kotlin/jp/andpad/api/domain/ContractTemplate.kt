package jp.andpad.api.domain

/**
 * 電子契約（e 契約）の契約書テンプレートを表すドメインモデル。
 *
 * 下請基本契約書、安全協力誓約書など、繰り返し使用する契約書の雛形を定義する。
 *
 * @property id テンプレートの一意識別子
 * @property name テンプレート名
 * @property body 契約書本文テンプレート（プレースホルダー含む）
 * @property createdAt テンプレート作成日時（ISO 8601 形式文字列）
 */
data class ContractTemplate(val id: String, val name: String, val body: String, val createdAt: String)
