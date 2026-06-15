package jp.andpad.api.graphql.input

import jp.andpad.api.domain.CostEntryType

data class CreateCostEntryInput(
    val projectId: String,
    val lineItemId: String,
    val entryType: CostEntryType,
    val vendorName: String,
    val description: String,
    val amount: Double,
    val entryDate: String,
    val invoiceNo: String,
    val recordedBy: String,
)
