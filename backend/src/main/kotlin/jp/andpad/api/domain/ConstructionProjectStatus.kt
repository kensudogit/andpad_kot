package jp.andpad.api.domain

/**
 * 建設工事案件のライフサイクルステータスを表す列挙型。
 *
 * 案件一覧、分析ダッシュボード、予算サマリなどで
 * 工事の進行段階を分類・集計するために使用する。
 */
enum class ConstructionProjectStatus {
    /** 計画中：着工前。見積・契約・設計段階 */
    PLANNING,

    /** 施工中：着工済みで工事が進行中 */
    IN_PROGRESS,

    /** 完了：竣工・引渡し済み */
    COMPLETED,

    /** 保留：一時停止中（天候・設計変更・資材不足など） */
    ON_HOLD
}
