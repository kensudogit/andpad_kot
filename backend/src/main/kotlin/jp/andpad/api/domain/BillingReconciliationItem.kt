package jp.andpad.api.domain

data class BillingReconciliationItem(
    val billingRecordId: String,
    val title: String,
    val billingAmount: Double,
    val costAmount: Double,
    val varianceAmount: Double,
    val status: String,
    val billingDate: String,
)
