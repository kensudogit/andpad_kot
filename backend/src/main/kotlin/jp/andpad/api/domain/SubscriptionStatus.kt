package jp.andpad.api.domain

/**
 * サブスクリプション（課金契約）の状態を表す列挙型。
 *
 * 組織の SaaS 利用可否、請求処理、機能制限の判定に使用する。
 */
enum class SubscriptionStatus {
    /** 有効：正常に課金・利用中 */
    ACTIVE,

    /** トライアル中：試用期間内で全機能または限定機能を利用可能 */
    TRIALING,

    /** 支払い遅延：請求の支払いが未完了。猶予期間中または機能制限中 */
    PAST_DUE,

    /** 解約済み：契約終了。利用停止または読み取り専用 */
    CANCELED
}
