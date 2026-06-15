package jp.andpad.api.domain

data class BudgetCategorySummary(
    val categoryCode: String,
    val categoryName: String,
    val budgetAmount: Double,
    val actualAmount: Double,
    val varianceAmount: Double,
)
