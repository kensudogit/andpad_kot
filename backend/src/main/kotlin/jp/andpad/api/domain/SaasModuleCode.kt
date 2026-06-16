package jp.andpad.api.domain

/**
 * ANDPAD SaaS プラットフォームの機能モジュール識別コードを表す列挙型。
 *
 * 組織ごとに有効化される建設 PM 機能（施工管理、黒板、BIM、原価管理など）を
 * 一意に識別し、モジュール別のレコード集計・利用分析に使用する。
 */
enum class SaasModuleCode {
    /** DX 推進：デジタル化施策の管理 */
    DX,

    /** CRM：顧客・商談管理 */
    CRM,

    /** 勤怠管理：出退勤・休暇申請 */
    ATTENDANCE,

    /** 電子契約：契約書の作成・署名 */
    ECONTRACT,

    /** AI チャットボット：現場相談・問合せ支援 */
    CHATBOT,

    /** ドキュメント RAG：社内文書検索・AI 回答 */
    DOC_RAG,

    /** 施工管理：工程・進捗・現場報告 */
    CONSTRUCTION_MGMT,

    /** 図面管理：設計図面の閲覧・共有 */
    DRAWINGS,

    /** 黒板：デジタル黒板・施工記録写真 */
    BLACKBOARD,

    /** 検査：品質検査・確認項目管理 */
    INSPECTION,

    /** プロジェクトボード：タスク・カンバン管理 */
    PROJECT_BOARD,

    /** 問合せ利益：見込み利益・収支シミュレーション */
    INQUIRY_PROFIT,

    /** 発注管理：資材・外注発注 */
    ORDERS,

    /** リモート現場：遠隔臨場・ライブ配信 */
    REMOTE_SITE,

    /** 書類承認：稟議・承認ワークフロー */
    DOC_APPROVAL,

    /** 3D スキャン：現場点群データ取込 */
    SCAN_3D,

    /** 請求管理：出来高請求・請求書発行 */
    BILLING,

    /** 歩掛管理：作業歩掛・人工算出 */
    WORK_RATE,

    /** 現場入退場：ゲート管理・入場者記録 */
    SITE_ACCESS,

    /** 電子納品：竣工図書の電子納品 */
    E_DELIVERY,

    /** ビルメンテナンス：建物維持管理 */
    BM,

    /** 分析：KPI ダッシュボード・レポート */
    ANALYTICS,

    /** API 連携：外部システムとのデータ連携 */
    API_INTEGRATION,

    /** BIM：3D モデル管理・ビューア */
    BIM,

    /** 原価管理：予算・実績・フォーキャスト */
    BUDGET_MGMT
}
