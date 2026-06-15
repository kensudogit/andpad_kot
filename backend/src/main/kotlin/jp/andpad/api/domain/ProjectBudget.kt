package jp.andpad.api.domain

data class ProjectBudget(
    val id: String,
    val projectId: String,
    val projectName: String,
    val name: String,
    val budgetType: BudgetType,
    val status: BudgetStatus,
    val versionNo: Int,
    val contractAmount: Double,
    val totalEstimate: Double,
    val totalBudget: Double,
    val totalCommitted: Double,
    val totalActual: Double,
    val notes: String,
    val approvedAt: String,
    val createdAt: String,
)
