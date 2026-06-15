package jp.andpad.api.service

import jp.andpad.api.domain.BudgetDashboard
import jp.andpad.api.domain.BudgetLineItem
import jp.andpad.api.domain.BudgetType
import jp.andpad.api.domain.CostEntry
import jp.andpad.api.domain.ProjectBudget
import jp.andpad.api.domain.ProjectBudgetSummary
import jp.andpad.api.graphql.input.CreateBudgetLineItemInput
import jp.andpad.api.graphql.input.CreateCostEntryInput
import jp.andpad.api.graphql.input.CreateProjectBudgetInput
import jp.andpad.api.repository.BudgetRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
) {

    fun listBudgets(projectId: String?, budgetType: BudgetType?): List<ProjectBudget> {
        return budgetRepository.listBudgets(TenantContext.orgId(), projectId, budgetType)
    }

    fun listLineItems(budgetId: String): List<BudgetLineItem> {
        return budgetRepository.listLineItems(TenantContext.orgId(), budgetId)
    }

    fun listCostEntries(projectId: String?, lineItemId: String?): List<CostEntry> {
        return budgetRepository.listCostEntries(TenantContext.orgId(), projectId, lineItemId)
    }

    fun budgetDashboard(projectId: String): BudgetDashboard {
        return budgetRepository.budgetDashboard(TenantContext.orgId(), projectId)
    }

    fun listBudgetSummaries(): List<ProjectBudgetSummary> {
        return budgetRepository.listBudgetSummaries(TenantContext.orgId())
    }

    fun createBudget(input: CreateProjectBudgetInput): ProjectBudget {
        return budgetRepository.createBudget(
            TenantContext.orgId(),
            input.projectId,
            input.name,
            input.budgetType,
            input.status,
            input.versionNo ?: 1,
            input.contractAmount ?: 0.0,
            input.notes,
        )
    }

    fun createLineItem(input: CreateBudgetLineItemInput): BudgetLineItem {
        return budgetRepository.createLineItem(
            TenantContext.orgId(),
            input.budgetId,
            input.categoryCode,
            input.categoryName,
            input.wbsCode,
            input.description,
            input.estimateAmount ?: 0.0,
            input.budgetAmount ?: 0.0,
            input.committedAmount ?: 0.0,
            input.sortOrder ?: 0,
        )
    }

    fun createCostEntry(input: CreateCostEntryInput): CostEntry {
        return budgetRepository.createCostEntry(
            TenantContext.orgId(),
            input.projectId,
            input.lineItemId,
            input.entryType,
            input.vendorName,
            input.description,
            input.amount,
            Dates.parseDate(input.entryDate),
            input.invoiceNo,
            input.recordedBy,
        )
    }

    fun approveBudget(id: String): ProjectBudget {
        return budgetRepository.approveBudget(TenantContext.orgId(), id)
    }

    fun createCostFromBilling(billingRecordId: String, projectId: String): CostEntry {
        return budgetRepository.createCostFromBilling(TenantContext.orgId(), billingRecordId, projectId)
    }
}
