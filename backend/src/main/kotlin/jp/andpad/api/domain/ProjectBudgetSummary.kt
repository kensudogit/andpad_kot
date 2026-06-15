package jp.andpad.api.domain

data class ProjectBudgetSummary(
    val projectId: String,
    val projectName: String,
    val status: ConstructionProjectStatus,
    val contractAmount: Double,
    val totalBudget: Double,
    val totalActual: Double,
    val billingTotal: Double,
    val variancePct: Double,
)
