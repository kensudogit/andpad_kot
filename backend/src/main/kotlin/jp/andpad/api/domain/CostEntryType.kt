package jp.andpad.api.domain

/**
 * 原価エントリ（原価計上明細）の費目種別を表す列挙型。
 *
 * 工事原価を材料費・労務費・外注費などに分類し、
 * 費目別集計・分析レポートの生成に使用する。
 */
enum class CostEntryType {
    /** 材料費：資材・部材の購入費 */
    MATERIAL,

    /** 労務費：自社作業員の人件費 */
    LABOR,

    /** 外注費：下請業者への発注費 */
    SUBCONTRACT,

    /** 機械経費：重機・設備のレンタル・運搬費 */
    EQUIPMENT,

    /** 間接費：現場管理費・共通費など */
    OVERHEAD,

    /** その他：上記に分類されない原価 */
    OTHER
}
