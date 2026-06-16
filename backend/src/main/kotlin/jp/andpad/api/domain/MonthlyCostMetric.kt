package jp.andpad.api.domain

/**
 * 月次原価メトリクスを表すドメインモデル。
 *
 * 原価管理ダッシュボードや分析画面で、月単位の原価推移をグラフ表示するための集計値。
 *
 * @property month 対象月（例: 「2026-01」）
 * @property amount 当該月の原価合計額（円）
 */
data class MonthlyCostMetric(val month: String, val amount: Double)
