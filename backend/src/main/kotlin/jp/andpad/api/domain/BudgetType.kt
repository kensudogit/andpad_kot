package jp.andpad.api.domain

/**
 * 工事案件予算の種別を表す列挙型。
 *
 * 見積段階から実行予算、完成時フォーキャストまで、
 * 原価管理のライフサイクルに応じた予算バージョンを区別する。
 */
enum class BudgetType {
    /** 見積：受注前または契約時の見積原価 */
    ESTIMATE,

    /** 実行予算：着工後に確定した実行用予算 */
    EXECUTION_BUDGET,

    /** フォーキャスト：完成時予想原価（実績ベースの予測） */
    FORECAST
}
