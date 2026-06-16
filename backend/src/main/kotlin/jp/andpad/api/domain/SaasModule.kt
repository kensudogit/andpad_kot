package jp.andpad.api.domain

/**
 * ANDPAD SaaS モジュールの定義と有効状態を表すドメインモデル。
 *
 * 組織ごとに契約・有効化されている機能モジュール（施工管理、黒板、請求など）の
 * メタ情報と ON/OFF 状態を保持する。
 *
 * @property code モジュール識別コード
 * @property name モジュールの表示名
 * @property description モジュールの機能説明
 * @property enabled 組織内で有効化されているか
 */
data class SaasModule(val code: SaasModuleCode, val name: String, val description: String, val enabled: Boolean)
