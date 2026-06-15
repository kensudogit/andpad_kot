package jp.andpad.api.graphql.input

import jp.andpad.api.domain.BudgetStatus
import jp.andpad.api.domain.BudgetType

data class CreateProjectBudgetInput(
    val projectId: String,
    val name: String,
    val budgetType: BudgetType,
    val status: BudgetStatus,
    val versionNo: Int?,
    val contractAmount: Double?,
    val notes: String,
)
