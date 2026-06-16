package jp.andpad.api.domain

/**
 * ANDPAD 建設 PM プラットフォームのテナント（組織）を表すドメインモデル。
 *
 * 元請建設会社や協力会社など、SaaS を契約して利用する法人・事業所単位のエンティティ。
 * プラン、サブスクリプション、有効モジュール、メンバー数などの契約情報を管理する。
 *
 * @property id 組織の一意識別子
 * @property name 組織名（会社名・事業所名）
 * @property slug URL 等で使用する組織スラッグ（一意の短縮識別子）
 * @property planTier 契約プランのティア
 * @property subscriptionStatus サブスクリプション（課金）の状態
 * @property seatCount 契約シート（利用可能ユーザー）数
 * @property timezone 組織のデフォルトタイムゾーン（例: 「Asia/Tokyo」）
 * @property memberCount 現在のメンバー数
 * @property createdAt 組織登録日時（ISO 8601 形式文字列）
 * @property enabledModules 組織で有効化されている SaaS モジュール一覧
 */
data class Organization(
    val id: String,
    val name: String,
    val slug: String,
    val planTier: PlanTier,
    val subscriptionStatus: SubscriptionStatus,
    val seatCount: Int,
    val timezone: String,
    val memberCount: Int,
    val createdAt: String,
    val enabledModules: List<SaasModule>,
)
