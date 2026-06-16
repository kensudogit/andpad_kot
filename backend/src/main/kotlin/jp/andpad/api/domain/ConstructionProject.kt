package jp.andpad.api.domain

/**
 * 建設工事案件（プロジェクト）を表すドメインモデル。
 *
 * ANDPAD PM の中核エンティティ。現場住所、工期、担当者、進捗ステータスなど、
 * 単一の建設プロジェクトに関する基本情報を保持する。
 *
 * @property id 工事案件の一意識別子
 * @property name 工事案件名（現場名）
 * @property siteAddress 工事現場の所在地
 * @property status 案件のライフサイクルステータス
 * @property managerName 現場代理人・プロジェクトマネージャー名
 * @property startDate 着工日（ISO 8601 日付文字列）
 * @property endDate 竣工予定日（ISO 8601 日付文字列）
 * @property recordCount 案件に紐づくモジュールレコード総数
 * @property createdAt 案件登録日時（ISO 8601 形式文字列）
 */
data class ConstructionProject(
    val id: String,
    val name: String,
    val siteAddress: String,
    val status: ConstructionProjectStatus,
    val managerName: String,
    val startDate: String,
    val endDate: String,
    val recordCount: Int,
    val createdAt: String,
)
