package jp.andpad.api.domain

data class CostEntry(
    val id: String,
    val projectId: String,
    val projectName: String,
    val lineItemId: String,
    val lineItemName: String,
    val entryType: CostEntryType,
    val vendorName: String,
    val description: String,
    val amount: Double,
    val entryDate: String,
    val invoiceNo: String,
    val recordedBy: String,
    val createdAt: String,
)
