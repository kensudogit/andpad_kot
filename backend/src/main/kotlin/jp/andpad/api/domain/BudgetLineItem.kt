package jp.andpad.api.domain

data class BudgetLineItem(
    val id: String,
    val budgetId: String,
    val categoryCode: String,
    val categoryName: String,
    val wbsCode: String,
    val description: String,
    val estimateAmount: Double,
    val budgetAmount: Double,
    val committedAmount: Double,
    val actualAmount: Double,
    val varianceAmount: Double,
    val variancePct: Double,
    val sortOrder: Int,
    val createdAt: String,
)
