package jp.andpad.api.domain

/**
 * API サービスのヘルスチェック結果を表すドメインモデル。
 *
 * ロードバランサや監視システム向けに、サービスの稼働状態とバージョン情報を返す。
 *
 * @property ok サービスが正常稼働中か
 * @property service サービス名（例: 「andpad-api」）
 * @property version デプロイされている API バージョン
 */
data class Health(val ok: Boolean, val service: String, val version: String)
