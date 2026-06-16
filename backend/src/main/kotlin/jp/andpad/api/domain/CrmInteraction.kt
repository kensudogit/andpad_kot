package jp.andpad.api.domain

/**
 * CRM の顧客接点（インタラクション）履歴 1 件を表すドメインモデル。
 *
 * 電話、メール、訪問、打合せなど、コンタクトとの接触記録を時系列で管理する。
 *
 * @property id インタラクションの一意識別子
 * @property contactId 対象コンタクト ID
 * @property kind 接点種別（例: 「call」「email」「meeting」「visit」）
 * @property summary 接点内容の要約
 * @property occurredAt 接点日時（ISO 8601 形式文字列）
 */
data class CrmInteraction(val id: String, val contactId: String, val kind: String, val summary: String, val occurredAt: String)
