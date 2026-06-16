package jp.andpad.api.domain

/**
 * 組織の SaaS 利用量サマリを表すドメインモデル。
 *
 * プラン上限に対するメンバー数、動画数、API 呼び出し数、
 * AI 相談トークン使用量などの消費状況を一覧化する。
 *
 * @property members 現在のメンバー数
 * @property membersLimit プラン上限メンバー数
 * @property videos 登録動画数
 * @property videosLimit プラン上限動画数
 * @property apiCallsThisMonth 当月の API 呼び出し回数
 * @property apiCallsLimit 当月の API 呼び出し上限回数
 * @property consultTokensMonth 当月の AI 相談トークン使用量
 */
data class UsageSummary(
    val members: Int,
    val membersLimit: Int,
    val videos: Int,
    val videosLimit: Int,
    val apiCallsThisMonth: Int,
    val apiCallsLimit: Int,
    val consultTokensMonth: Int,
)
