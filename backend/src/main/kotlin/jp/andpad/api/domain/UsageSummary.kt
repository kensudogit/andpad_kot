package jp.andpad.api.domain

data class UsageSummary(
    val members: Int,
    val membersLimit: Int,
    val videos: Int,
    val videosLimit: Int,
    val apiCallsThisMonth: Int,
    val apiCallsLimit: Int,
    val consultTokensMonth: Int,
)
