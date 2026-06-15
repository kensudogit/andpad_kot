package jp.andpad.api.graphql.input

data class CreateBudgetLineItemInput(
    val budgetId: String,
    val categoryCode: String,
    val categoryName: String,
    val wbsCode: String,
    val description: String,
    val estimateAmount: Double?,
    val budgetAmount: Double?,
    val committedAmount: Double?,
    val sortOrder: Int?,
)
