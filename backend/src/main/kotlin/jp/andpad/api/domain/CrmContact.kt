package jp.andpad.api.domain

/**
 * CRM（顧客関係管理）の取引先・見込み客コンタクトを表すドメインモデル。
 *
 * 発注者、設計事務所、協力会社担当者など、営業・受注活動の対象となる
 * 人物・法人の連絡先情報と商談ステージを管理する。
 *
 * @property id コンタクトの一意識別子
 * @property name 担当者名または法人名
 * @property email メールアドレス
 * @property phone 電話番号
 * @property company 所属会社名
 * @property stage 商談・リードステージ（例: 「lead」「proposal」「won」）
 * @property notes 備考・メモ
 * @property createdAt 登録日時（ISO 8601 形式文字列）
 */
data class CrmContact(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val company: String,
    val stage: String,
    val notes: String,
    val createdAt: String,
)
