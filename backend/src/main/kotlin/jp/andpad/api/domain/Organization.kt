package jp.andpad.api.domain

data class Organization(
    val id: String,
    val name: String,
    val slug: String,
    val planTier: PlanTier,
    val subscriptionStatus: SubscriptionStatus,
    val seatCount: Int,
    val timezone: String,
    val memberCount: Int,
    val createdAt: String,
    val enabledModules: List<SaasModule>,
)
