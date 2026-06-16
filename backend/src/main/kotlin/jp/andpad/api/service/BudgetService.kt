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

/**
 * 予算・原価管理サービス。
 *
 * **責務**: GraphQL / REST 入力を [BudgetRepository] 呼び出しに変換。
 * orgId は [TenantContext] から自動注入。
 *
 * **テナント分離**: 全メソッドで TenantContext.orgId() をリポジトリに渡す。
 */
@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
) {

    /** 予算一覧（projectId / budgetType フィルタ可）。 */
    fun listBudgets(projectId: String?, budgetType: BudgetType?): List<ProjectBudget> {
        return budgetRepository.listBudgets(TenantContext.orgId(), projectId, budgetType)
    }

    /** 指定予算の明細行一覧。 */
    fun listLineItems(budgetId: String): List<BudgetLineItem> {
        return budgetRepository.listLineItems(TenantContext.orgId(), budgetId)
    }

    /** 原価エントリ一覧（projectId 必須）。 */
    fun listCostEntries(projectId: String?, lineItemId: String?): List<CostEntry> {
        return budgetRepository.listCostEntries(TenantContext.orgId(), projectId, lineItemId)
    }

    /** プロジェクト予算ダッシュボード集計。 */
    fun budgetDashboard(projectId: String): BudgetDashboard {
        return budgetRepository.budgetDashboard(TenantContext.orgId(), projectId)
    }

    /** 全プロジェクトの予算サマリ一覧。 */
    fun listBudgetSummaries(): List<ProjectBudgetSummary> {
        return budgetRepository.listBudgetSummaries(TenantContext.orgId())
    }

    /** 新規予算作成（versionNo 省略時 1、contractAmount 省略時 0）。 */
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

    /** 予算明細行作成（金額省略時 0）。 */
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

    /** 原価エントリ作成（entryDate 文字列を LocalDate に変換）。 */
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

    /** 予算承認（status → APPROVED）。 */
    fun approveBudget(id: String): ProjectBudget {
        return budgetRepository.approveBudget(TenantContext.orgId(), id)
    }

    /** 請求モジュールレコードから原価エントリを自動起票。 */
    fun createCostFromBilling(billingRecordId: String, projectId: String): CostEntry {
        return budgetRepository.createCostFromBilling(TenantContext.orgId(), billingRecordId, projectId)
    }
}
