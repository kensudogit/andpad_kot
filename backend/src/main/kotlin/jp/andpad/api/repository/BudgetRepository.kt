package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import jp.andpad.api.domain.BillingReconciliationItem
import jp.andpad.api.domain.BudgetCategorySummary
import jp.andpad.api.domain.BudgetDashboard
import jp.andpad.api.domain.BudgetLineItem
import jp.andpad.api.domain.BudgetStatus
import jp.andpad.api.domain.BudgetType
import jp.andpad.api.domain.ConstructionProjectStatus
import jp.andpad.api.domain.CostEntry
import jp.andpad.api.domain.CostEntryType
import jp.andpad.api.domain.MonthlyCostMetric
import jp.andpad.api.domain.ProjectBudget
import jp.andpad.api.domain.ProjectBudgetSummary
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class BudgetRepository(
    private val jdbc: JdbcTemplate,
) {

    fun listBudgets(orgId: String, projectId: String?, budgetType: BudgetType?): List<ProjectBudget> {
        val sql = StringBuilder(
            """
            SELECT b.id, b.project_id, p.name AS project_name, b.name, b.budget_type, b.status,
                   b.version_no, b.contract_amount, b.notes, b.approved_at, b.created_at
            FROM project_budgets b
            JOIN construction_projects p ON p.id = b.project_id
            WHERE b.org_id = ?
            """.trimIndent(),
        )
        val args = ArrayList<Any>()
        args.add(orgId)
        if (!projectId.isNullOrBlank()) {
            sql.append(" AND b.project_id = ?")
            args.add(projectId)
        }
        if (budgetType != null) {
            sql.append(" AND b.budget_type = ?")
            args.add(budgetType.name)
        }
        sql.append(" ORDER BY b.created_at DESC")
        val budgets = jdbc.query(
            sql.toString(),
            { rs, _ -> mapBudget(rs) },
            *args.toTypedArray(),
        )
        return budgets.map { fillTotals(orgId, it) }
    }

    fun listLineItems(orgId: String, budgetId: String): List<BudgetLineItem> {
        return jdbc.query(
            """
            SELECT id, budget_id, category_code, category_name, wbs_code, description,
                   estimate_amount, budget_amount, committed_amount, actual_amount, sort_order, created_at
            FROM budget_line_items
            WHERE org_id = ? AND budget_id = ?
            ORDER BY sort_order, created_at
            """.trimIndent(),
            { rs, _ -> mapLineItem(rs) },
            orgId,
            budgetId,
        )
    }

    fun listCostEntries(orgId: String, projectId: String?, lineItemId: String?): List<CostEntry> {
        if (projectId.isNullOrBlank()) {
            return emptyList()
        }
        val sql = StringBuilder(
            """
            SELECT c.id, c.project_id, p.name AS project_name, COALESCE(c.line_item_id, '') AS line_item_id,
                   COALESCE(l.category_name || ' ' || l.description, '') AS line_item_name,
                   c.entry_type, c.vendor_name, c.description, c.amount, c.entry_date,
                   c.invoice_no, c.recorded_by, c.created_at
            FROM cost_entries c
            JOIN construction_projects p ON p.id = c.project_id
            LEFT JOIN budget_line_items l ON l.id = c.line_item_id
            WHERE c.org_id = ? AND c.project_id = ?
            """.trimIndent(),
        )
        val args = ArrayList<Any>()
        args.add(orgId)
        args.add(projectId)
        if (!lineItemId.isNullOrBlank()) {
            sql.append(" AND c.line_item_id = ?")
            args.add(lineItemId)
        }
        sql.append(" ORDER BY c.entry_date DESC, c.created_at DESC")
        return jdbc.query(sql.toString(), { rs, _ -> mapCostEntry(rs) }, *args.toTypedArray())
    }

    fun budgetDashboard(orgId: String, projectId: String): BudgetDashboard {
        val projectName = jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            projectId,
            orgId,
        )!!
        val empty = emptyDashboard(projectId, projectName)
        return try {
            val row = jdbc.queryForMap(
                """
                SELECT id, contract_amount FROM project_budgets
                WHERE org_id = ? AND project_id = ? AND budget_type = 'EXECUTION_BUDGET'
                ORDER BY version_no DESC, created_at DESC LIMIT 1
                """.trimIndent(),
                orgId,
                projectId,
            )
            val budgetId = row["id"] as String
            val contractAmount = (row["contract_amount"] as Number).toDouble()
            val lineItems = listLineItems(orgId, budgetId)
            val catMap = HashMap<String, BudgetCategorySummary>()
            var totalEstimate = 0.0
            var totalBudget = 0.0
            var totalCommitted = 0.0
            var totalActual = 0.0
            for (item in lineItems) {
                totalEstimate += item.estimateAmount
                totalBudget += item.budgetAmount
                totalCommitted += item.committedAmount
                totalActual += item.actualAmount
                catMap.compute(item.categoryCode) { _, v ->
                    if (v == null) {
                        BudgetCategorySummary(
                            item.categoryCode,
                            item.categoryName,
                            item.budgetAmount,
                            item.actualAmount,
                            item.budgetAmount - item.actualAmount,
                        )
                    } else {
                        BudgetCategorySummary(
                            v.categoryCode,
                            v.categoryName,
                            v.budgetAmount + item.budgetAmount,
                            v.actualAmount + item.actualAmount,
                            v.budgetAmount + item.budgetAmount - v.actualAmount - item.actualAmount,
                        )
                    }
                }
            }
            val totalForecast = totalActual + (totalBudget - totalCommitted)
            val varianceAmount = totalBudget - totalActual
            val variancePct = if (totalBudget > 0) varianceAmount / totalBudget * 100 else 0.0
            val completionPct = if (totalBudget > 0) totalActual / totalBudget * 100 else 0.0
            val grossMarginPct = if (contractAmount > 0) (contractAmount - totalBudget) / contractAmount * 100 else 0.0
            val estimateBudgetTotal = queryEstimateTotal(orgId, projectId)
            val inquiryProfitTotal = sumModuleAmount(orgId, projectId, "INQUIRY_PROFIT")
            val billingTotal = sumModuleAmount(orgId, projectId, "BILLING")
            val costs = listCostEntries(orgId, projectId, null)
            val recent = if (costs.size > 10) costs.subList(0, 10) else costs
            BudgetDashboard(
                projectId,
                projectName,
                contractAmount,
                totalEstimate,
                totalBudget,
                totalCommitted,
                totalActual,
                totalForecast,
                varianceAmount,
                variancePct,
                completionPct,
                estimateBudgetTotal,
                grossMarginPct,
                inquiryProfitTotal,
                billingTotal,
                billingTotal - totalActual,
                monthlyCosts(orgId, projectId, 6),
                billingReconciliation(orgId, projectId),
                lineItems,
                recent,
                ArrayList(catMap.values),
                Dates.formatRequired(Dates.now()),
            )
        } catch (_: EmptyResultDataAccessException) {
            empty
        }
    }

    fun listBudgetSummaries(orgId: String): List<ProjectBudgetSummary> {
        val summaries = jdbc.query(
            """
            SELECT p.id, p.name, p.status,
                   COALESCE(MAX(b.contract_amount), 0) AS contract_amount,
                   COALESCE(SUM(li.budget_amount), 0) AS total_budget,
                   COALESCE(SUM(li.actual_amount), 0) AS total_actual
            FROM construction_projects p
            LEFT JOIN project_budgets b ON b.project_id = p.id AND b.org_id = p.org_id
                AND b.budget_type = 'EXECUTION_BUDGET' AND b.status = 'APPROVED'
            LEFT JOIN budget_line_items li ON li.budget_id = b.id
            WHERE p.org_id = ?
            GROUP BY p.id, p.name, p.status
            ORDER BY p.name
            """.trimIndent(),
            { rs, _ ->
                val totalBudget = rs.getDouble("total_budget")
                val totalActual = rs.getDouble("total_actual")
                val variancePct = if (totalBudget > 0) (totalBudget - totalActual) / totalBudget * 100 else 0.0
                ProjectBudgetSummary(
                    rs.requireStr("id"),
                    rs.requireStr("name"),
                    ConstructionProjectStatus.valueOf(rs.requireStr("status")),
                    rs.getDouble("contract_amount"),
                    totalBudget,
                    totalActual,
                    0.0,
                    variancePct,
                )
            },
            orgId,
        )
        return summaries.map { summary ->
            ProjectBudgetSummary(
                summary.projectId,
                summary.projectName,
                summary.status,
                summary.contractAmount,
                summary.totalBudget,
                summary.totalActual,
                sumModuleAmount(orgId, summary.projectId, "BILLING"),
                summary.variancePct,
            )
        }
    }

    @Transactional
    fun createBudget(
        orgId: String,
        projectId: String,
        name: String,
        budgetType: BudgetType?,
        status: BudgetStatus?,
        versionNo: Int,
        contractAmount: Double,
        notes: String?,
    ): ProjectBudget {
        val id = Ids.random("bud_")
        val bt = budgetType ?: BudgetType.EXECUTION_BUDGET
        val st = status ?: BudgetStatus.DRAFT
        val version = if (versionNo <= 0) 1 else versionNo
        val projectName = jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            projectId,
            orgId,
        )!!
        jdbc.update(
            """
            INSERT INTO project_budgets (id, org_id, project_id, name, budget_type, status, version_no,
                contract_amount, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            projectId,
            name,
            bt.name,
            st.name,
            version,
            contractAmount,
            notes ?: "",
        )
        return ProjectBudget(
            id,
            projectId,
            projectName,
            name,
            bt,
            st,
            version,
            contractAmount,
            0.0,
            0.0,
            0.0,
            0.0,
            notes ?: "",
            "",
            Dates.formatRequired(Dates.now()),
        )
    }

    @Transactional
    fun createLineItem(
        orgId: String,
        budgetId: String,
        categoryCode: String,
        categoryName: String,
        wbsCode: String?,
        description: String?,
        estimateAmount: Double,
        budgetAmount: Double,
        committedAmount: Double,
        sortOrder: Int,
    ): BudgetLineItem {
        val id = Ids.random("bli_")
        jdbc.update(
            """
            INSERT INTO budget_line_items (id, org_id, budget_id, category_code, category_name, wbs_code,
                description, estimate_amount, budget_amount, committed_amount, sort_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            budgetId,
            categoryCode,
            categoryName,
            wbsCode ?: "",
            description ?: "",
            estimateAmount,
            budgetAmount,
            committedAmount,
            sortOrder,
        )
        return mapLineItemVariance(
            BudgetLineItem(
                id,
                budgetId,
                categoryCode,
                categoryName,
                wbsCode ?: "",
                description ?: "",
                estimateAmount,
                budgetAmount,
                committedAmount,
                0.0,
                0.0,
                0.0,
                sortOrder,
                Dates.formatRequired(Dates.now()),
            ),
        )
    }

    @Transactional
    fun createCostEntry(
        orgId: String,
        projectId: String,
        lineItemId: String?,
        entryType: CostEntryType?,
        vendorName: String?,
        description: String,
        amount: Double,
        entryDate: LocalDate?,
        invoiceNo: String?,
        recordedBy: String?,
    ): CostEntry {
        val id = Ids.random("cost_")
        val et = entryType ?: CostEntryType.OTHER
        val date = entryDate ?: LocalDate.now()
        val projectName = jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            projectId,
            orgId,
        )!!
        jdbc.update(
            """
            INSERT INTO cost_entries (id, org_id, project_id, line_item_id, entry_type, vendor_name,
                description, amount, entry_date, invoice_no, recorded_by)
            VALUES (?, ?, ?, NULLIF(?, ''), ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            projectId,
            lineItemId ?: "",
            et.name,
            vendorName ?: "",
            description,
            amount,
            Date.valueOf(date),
            invoiceNo ?: "",
            recordedBy ?: "",
        )
        if (!lineItemId.isNullOrBlank()) {
            jdbc.update(
                "UPDATE budget_line_items SET actual_amount = actual_amount + ? WHERE id = ? AND org_id = ?",
                amount,
                lineItemId,
                orgId,
            )
        }
        var lineItemName = ""
        if (!lineItemId.isNullOrBlank()) {
            try {
                lineItemName = jdbc.queryForObject(
                    "SELECT category_name || ' ' || description FROM budget_line_items WHERE id = ?",
                    String::class.java,
                    lineItemId,
                )!!
            } catch (_: EmptyResultDataAccessException) {
            }
        }
        return CostEntry(
            id,
            projectId,
            projectName,
            lineItemId ?: "",
            lineItemName,
            et,
            vendorName ?: "",
            description,
            amount,
            date.toString(),
            invoiceNo ?: "",
            recordedBy ?: "",
            Dates.formatRequired(Dates.now()),
        )
    }

    @Transactional
    fun approveBudget(orgId: String, id: String): ProjectBudget {
        jdbc.update(
            "UPDATE project_budgets SET status = 'APPROVED', approved_at = NOW() WHERE id = ? AND org_id = ?",
            id,
            orgId,
        )
        val budget = jdbc.queryForMap(
            """
            SELECT b.id, b.project_id, p.name AS project_name, b.name, b.budget_type, b.status,
                   b.version_no, b.contract_amount, b.notes, b.approved_at, b.created_at
            FROM project_budgets b
            JOIN construction_projects p ON p.id = b.project_id
            WHERE b.id = ? AND b.org_id = ?
            """.trimIndent(),
            id,
            orgId,
        )
        return fillTotals(orgId, mapBudgetFromMap(budget))
    }

    @Transactional
    fun createCostFromBilling(orgId: String, billingRecordId: String, projectId: String): CostEntry {
        val row = jdbc.queryForMap(
            """
            SELECT title, detail, COALESCE(amount, 0) AS amount, record_date
            FROM project_module_records
            WHERE id = ? AND org_id = ? AND project_id = ? AND module_code = 'BILLING'
            """.trimIndent(),
            billingRecordId,
            orgId,
            projectId,
        )
        val title = row["title"] as String
        val detail = row["detail"] as String?
        val amount = (row["amount"] as Number).toDouble()
        val entryDate = when (val recordDate = row["record_date"]) {
            is Date -> recordDate.toLocalDate()
            else -> LocalDate.now()
        }
        val desc = if (detail.isNullOrBlank()) title else "$title — $detail"
        return createCostEntry(
            orgId,
            projectId,
            null,
            CostEntryType.OTHER,
            "請求連携",
            desc,
            amount,
            entryDate,
            "BILL-$billingRecordId",
            null,
        )
    }

    private fun fillTotals(orgId: String, budget: ProjectBudget): ProjectBudget {
        val totals = jdbc.queryForMap(
            """
            SELECT COALESCE(SUM(estimate_amount), 0) AS total_estimate,
                   COALESCE(SUM(budget_amount), 0) AS total_budget,
                   COALESCE(SUM(committed_amount), 0) AS total_committed,
                   COALESCE(SUM(actual_amount), 0) AS total_actual
            FROM budget_line_items WHERE budget_id = ? AND org_id = ?
            """.trimIndent(),
            budget.id,
            orgId,
        )
        return ProjectBudget(
            budget.id,
            budget.projectId,
            budget.projectName,
            budget.name,
            budget.budgetType,
            budget.status,
            budget.versionNo,
            budget.contractAmount,
            (totals["total_estimate"] as Number).toDouble(),
            (totals["total_budget"] as Number).toDouble(),
            (totals["total_committed"] as Number).toDouble(),
            (totals["total_actual"] as Number).toDouble(),
            budget.notes,
            budget.approvedAt,
            budget.createdAt,
        )
    }

    private fun mapBudget(rs: java.sql.ResultSet): ProjectBudget {
        return ProjectBudget(
            rs.requireStr("id"),
            rs.requireStr("project_id"),
            rs.requireStr("project_name"),
            rs.requireStr("name"),
            BudgetType.valueOf(rs.requireStr("budget_type")),
            BudgetStatus.valueOf(rs.requireStr("status")),
            rs.getInt("version_no"),
            rs.getDouble("contract_amount"),
            0.0,
            0.0,
            0.0,
            0.0,
            rs.requireStr("notes"),
            if (rs.getTimestamp("approved_at") == null) "" else formatTimestamp(rs.getTimestamp("approved_at")),
            formatTimestamp(rs.getTimestamp("created_at")),
        )
    }

    private fun mapBudgetFromMap(row: Map<String, Any?>): ProjectBudget {
        val approved = when (val approvedAt = row["approved_at"]) {
            is Timestamp -> approvedAt.toInstant()
            else -> null
        }
        val created = when (val createdAt = row["created_at"]) {
            is Timestamp -> createdAt.toInstant()
            else -> Dates.now()
        }
        return ProjectBudget(
            row["id"] as String,
            row["project_id"] as String,
            row["project_name"] as String,
            row["name"] as String,
            BudgetType.valueOf(row["budget_type"] as String),
            BudgetStatus.valueOf(row["status"] as String),
            (row["version_no"] as Number).toInt(),
            (row["contract_amount"] as Number).toDouble(),
            0.0,
            0.0,
            0.0,
            0.0,
            row["notes"] as String,
            if (approved == null) "" else Dates.formatRequired(approved),
            Dates.formatRequired(created),
        )
    }

    private fun mapLineItem(rs: java.sql.ResultSet): BudgetLineItem {
        return mapLineItemVariance(
            BudgetLineItem(
                rs.requireStr("id"),
                rs.requireStr("budget_id"),
                rs.requireStr("category_code"),
                rs.requireStr("category_name"),
                rs.requireStr("wbs_code"),
                rs.requireStr("description"),
                rs.getDouble("estimate_amount"),
                rs.getDouble("budget_amount"),
                rs.getDouble("committed_amount"),
                rs.getDouble("actual_amount"),
                0.0,
                0.0,
                rs.getInt("sort_order"),
                formatTimestamp(rs.getTimestamp("created_at")),
            ),
        )
    }

    private fun mapLineItemVariance(item: BudgetLineItem): BudgetLineItem {
        val variance = item.budgetAmount - item.actualAmount
        val variancePct = if (item.budgetAmount > 0) variance / item.budgetAmount * 100 else 0.0
        return BudgetLineItem(
            item.id,
            item.budgetId,
            item.categoryCode,
            item.categoryName,
            item.wbsCode,
            item.description,
            item.estimateAmount,
            item.budgetAmount,
            item.committedAmount,
            item.actualAmount,
            variance,
            variancePct,
            item.sortOrder,
            item.createdAt,
        )
    }

    private fun mapCostEntry(rs: java.sql.ResultSet): CostEntry {
        return CostEntry(
            rs.requireStr("id"),
            rs.requireStr("project_id"),
            rs.requireStr("project_name"),
            rs.requireStr("line_item_id"),
            rs.requireStr("line_item_name"),
            CostEntryType.valueOf(rs.requireStr("entry_type")),
            rs.requireStr("vendor_name"),
            rs.requireStr("description"),
            rs.getDouble("amount"),
            formatDate(rs.getDate("entry_date")) ?: "",
            rs.requireStr("invoice_no"),
            rs.requireStr("recorded_by"),
            formatTimestamp(rs.getTimestamp("created_at")),
        )
    }

    private fun queryEstimateTotal(orgId: String, projectId: String): Double {
        return try {
            jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(bli.budget_amount), 0) FROM budget_line_items bli
                WHERE bli.org_id = ? AND bli.budget_id = (
                    SELECT id FROM project_budgets
                    WHERE org_id = ? AND project_id = ? AND budget_type = 'ESTIMATE'
                    ORDER BY version_no DESC LIMIT 1
                )
                """.trimIndent(),
                Double::class.java,
                orgId,
                orgId,
                projectId,
            )!!
        } catch (_: EmptyResultDataAccessException) {
            0.0
        }
    }

    private fun sumModuleAmount(orgId: String, projectId: String, moduleCode: String): Double {
        return try {
            jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(amount), 0) FROM project_module_records
                WHERE org_id = ? AND project_id = ? AND module_code = ?
                """.trimIndent(),
                Double::class.java,
                orgId,
                projectId,
                moduleCode,
            )!!
        } catch (_: EmptyResultDataAccessException) {
            0.0
        }
    }

    private fun monthlyCosts(orgId: String, projectId: String, months: Int): List<MonthlyCostMetric> {
        val since = LocalDate.now().minusMonths((months - 1).toLong()).withDayOfMonth(1)
        val byMonth = HashMap<String, Double>()
        jdbc.query(
            """
            SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                   COALESCE(SUM(amount), 0) AS amount
            FROM cost_entries
            WHERE org_id = ? AND project_id = ? AND entry_date >= ?
            GROUP BY 1 ORDER BY 1
            """.trimIndent(),
            { rs ->
                byMonth[rs.requireStr("month")] = rs.getDouble("amount")
            },
            orgId,
            projectId,
            Date.valueOf(since),
        )
        val out = ArrayList<MonthlyCostMetric>()
        val start = YearMonth.from(since)
        for (i in 0 until months) {
            val key = start.plusMonths(i.toLong()).toString()
            out.add(MonthlyCostMetric(key, byMonth.getOrDefault(key, 0.0)))
        }
        return out
    }

    private fun billingReconciliation(orgId: String, projectId: String): List<BillingReconciliationItem> {
        val costByMonth = HashMap<String, Double>()
        jdbc.query(
            """
            SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                   COALESCE(SUM(amount), 0) AS amount
            FROM cost_entries WHERE org_id = ? AND project_id = ?
            GROUP BY 1
            """.trimIndent(),
            { rs ->
                while (rs.next()) {
                    costByMonth[rs.requireStr("month")] = rs.getDouble("amount")
                }
            },
            orgId,
            projectId,
        )
        return jdbc.query(
            """
            SELECT id, title, COALESCE(amount, 0) AS amount, record_date
            FROM project_module_records
            WHERE org_id = ? AND project_id = ? AND module_code = 'BILLING'
            ORDER BY record_date DESC NULLS LAST, created_at DESC
            """.trimIndent(),
            { rs, _ ->
                val billingAmount = rs.getDouble("amount")
                var monthKey = ""
                var billingDate: String? = null
                if (rs.getDate("record_date") != null) {
                    val d = rs.getDate("record_date").toLocalDate()
                    billingDate = d.toString()
                    monthKey = YearMonth.from(d).toString()
                }
                val costAmount = costByMonth.getOrDefault(monthKey, 0.0)
                val variance = billingAmount - costAmount
                val status = when {
                    billingAmount == 0.0 -> "NONE"
                    costAmount == 0.0 -> "UNMATCHED"
                    kotlin.math.abs(variance / billingAmount * 100) <= 5 -> "MATCHED"
                    variance > 0 -> "UNDER"
                    else -> "OVER"
                }
                BillingReconciliationItem(
                    rs.requireStr("id"),
                    rs.requireStr("title"),
                    billingAmount,
                    costAmount,
                    variance,
                    status,
                    billingDate ?: "",
                )
            },
            orgId,
            projectId,
        )
    }

    private fun emptyDashboard(projectId: String, projectName: String): BudgetDashboard {
        return BudgetDashboard(
            projectId,
            projectName,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            emptyList(),
            Dates.formatRequired(Dates.now()),
        )
    }

    private fun formatTimestamp(ts: Timestamp?): String {
        return if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
    }

    private fun formatDate(date: Date?): String? {
        return date?.toLocalDate()?.toString()
    }
}
