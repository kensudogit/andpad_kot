package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import jp.andpad.api.domain.ConstructionProjectStatus
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsKpi
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration
import jp.andpad.api.domain.ExtendedTypes.BimModel
import jp.andpad.api.domain.ExtendedTypes.ModuleUsageMetric
import jp.andpad.api.domain.ExtendedTypes.ProjectStatusCount
import jp.andpad.api.domain.MonthlyCostMetric
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 拡張機能（分析・API 連携・BIM）の JDBC リポジトリ。
 *
 * **責務**: ANDPAD 風分析ダッシュボード集計、外部 API 連携設定、BIM モデル管理。
 *
 * **参照テーブル**: [construction_projects], [project_module_records], [budget_line_items],
 * [cost_entries], [saas_modules], [api_integrations], [bim_models]
 *
 * **テナント分離**: 全クエリで `org_id = ?` を必須とする。
 */
@Repository
class ExtendedRepository(
    private val jdbc: JdbcTemplate,
) {

    /**
     * 指定期間の組織横断分析ダッシュボードを集計する。
     *
     * KPI（進行中案件・請求・予算・原価等）、ステータス別件数、モジュール利用 TOP10、
     * 週次レコード推移、プロジェクト健全性スコア、月次原価を算出。
     *
     * @param periodDays 集計日数（≤ 0 時は 30 日）
     */
    fun andpadAnalytics(orgId: String, periodDays: Int): AndpadAnalyticsDashboard {
        val days = if (periodDays <= 0) 30 else periodDays
        val since = Instant.now().minusSeconds(days.toLong() * 86400L)
        val sinceTs = Timestamp.from(since)

        // 進行中プロジェクト件数
        val active = queryInt(
            """
            SELECT COUNT(*) FROM construction_projects
            WHERE org_id = ? AND status = 'IN_PROGRESS'
            """.trimIndent(),
            orgId,
        )

        // 期間内 BILLING モジュール amount 合計
        val billingTotal = queryDouble(
            """
            SELECT COALESCE(SUM(amount), 0) FROM project_module_records
            WHERE org_id = ? AND module_code = 'BILLING' AND created_at >= ?
            """.trimIndent(),
            orgId,
            sinceTs,
        )

        val projectsByStatus = jdbc.query(
            """
            SELECT status, COUNT(*)::int FROM construction_projects
            WHERE org_id = ? GROUP BY status
            """.trimIndent(),
            { rs, _ ->
                ProjectStatusCount(
                    ConstructionProjectStatus.valueOf(rs.requireStr("status")),
                    rs.getInt("count"),
                )
            },
            orgId,
        )

        val moduleUsage = jdbc.query(
            """
            SELECT r.module_code, COALESCE(m.name, r.module_code) AS module_name, COUNT(*)::int
            FROM project_module_records r
            LEFT JOIN saas_modules m ON m.code = r.module_code
            WHERE r.org_id = ? AND r.created_at >= ?
            GROUP BY r.module_code, m.name
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """.trimIndent(),
            { rs, _ ->
                ModuleUsageMetric(
                    SaasModuleCode.valueOf(rs.requireStr("module_code")),
                    rs.requireStr("module_name"),
                    rs.getInt("count"),
                )
            },
            orgId,
            sinceTs,
        )

        val totalRecords = queryInt(
            """
            SELECT COUNT(*) FROM project_module_records
            WHERE org_id = ? AND created_at >= ?
            """.trimIndent(),
            orgId,
            sinceTs,
        )

        val totalProjects = queryInt(
            "SELECT COUNT(*) FROM construction_projects WHERE org_id = ?",
            orgId,
        )

        val recordsByWeek = ArrayList<Double>(4)
        val now = Instant.now()
        for (i in 0 until 4) {
            val weekStart = now.minusSeconds(7L * (4 - i) * 86400L)
            val weekEnd = now.minusSeconds(7L * (3 - i) * 86400L)
            recordsByWeek.add(
                queryInt(
                    """
                    SELECT COUNT(*) FROM project_module_records
                    WHERE org_id = ? AND created_at >= ? AND created_at < ?
                    """.trimIndent(),
                    orgId,
                    Timestamp.from(weekStart),
                    Timestamp.from(weekEnd),
                ).toDouble(),
            )
        }

        var inProgress = 0
        var completed = 0
        var onHold = 0
        for (s in projectsByStatus) {
            when (s.status) {
                ConstructionProjectStatus.IN_PROGRESS -> inProgress = s.count
                ConstructionProjectStatus.COMPLETED -> completed = s.count
                ConstructionProjectStatus.ON_HOLD -> onHold = s.count
                else -> {}
            }
        }

        var projectHealthScore = 0.0
        if (totalProjects > 0) {
            projectHealthScore = (inProgress + completed).toDouble() / totalProjects * 100
            projectHealthScore -= onHold.toDouble() / totalProjects * 25
            projectHealthScore = maxOf(0.0, minOf(100.0, projectHealthScore))
        }

        val budgetTotal = queryDouble(
            """
            SELECT COALESCE(SUM(budget_amount), 0) FROM budget_line_items WHERE org_id = ?
            """.trimIndent(),
            orgId,
        )
        val actualSum = queryDouble(
            """
            SELECT COALESCE(SUM(actual_amount), 0) FROM budget_line_items WHERE org_id = ?
            """.trimIndent(),
            orgId,
        )
        val budgetVariancePct = if (budgetTotal > 0) (budgetTotal - actualSum) / budgetTotal * 100 else 0.0

        val costTotal = queryDouble(
            """
            SELECT COALESCE(SUM(amount), 0) FROM cost_entries
            WHERE org_id = ? AND entry_date >= ?
            """.trimIndent(),
            orgId,
            Date.valueOf(since.atOffset(ZoneOffset.UTC).toLocalDate()),
        )

        val costByMonth = orgMonthlyCosts(orgId, 6)
        val trend = 5.2

        val kpis = listOf(
            AndpadAnalyticsKpi("進行中案件", active.toDouble(), "件", trend),
            AndpadAnalyticsKpi("登録案件", totalProjects.toDouble(), "件", null),
            AndpadAnalyticsKpi("期間内記録", totalRecords.toDouble(), "件", null),
            AndpadAnalyticsKpi("請求合計", billingTotal, "円", null),
            AndpadAnalyticsKpi("実行予算合計", budgetTotal, "円", null),
            AndpadAnalyticsKpi("期間内原価", costTotal, "円", null),
            AndpadAnalyticsKpi("予算差異率", budgetVariancePct, "%", null),
        )

        return AndpadAnalyticsDashboard(
            days,
            kpis,
            projectsByStatus,
            moduleUsage,
            billingTotal,
            active,
            recordsByWeek,
            projectHealthScore,
            budgetTotal,
            costTotal,
            budgetVariancePct,
            costByMonth,
            Dates.formatRequired(Dates.now()),
        )
    }

    /** 組織の API 連携設定一覧（作成日降順）。 */
    fun listApiIntegrations(orgId: String): List<ApiIntegration> {
        return jdbc.query(
            """
            SELECT id, name, provider, endpoint_url, api_key_hint, status, last_sync_at, created_at
            FROM api_integrations WHERE org_id = ? ORDER BY created_at DESC
            """.trimIndent(),
            { rs, _ -> mapApiIntegration(rs) },
            orgId,
        )
    }

    /**
     * API 連携設定を INSERT（status = ACTIVE）。
     *
     * @return 作成直後の [ApiIntegration]
     */
    @Transactional
    fun createApiIntegration(orgId: String, input: CreateApiIntegrationInput): ApiIntegration {
        val id = Ids.random("api_")
        jdbc.update(
            """
            INSERT INTO api_integrations (id, org_id, name, provider, endpoint_url, api_key_hint, status)
            VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
            """.trimIndent(),
            id,
            orgId,
            input.name,
            input.provider ?: "",
            input.endpointUrl ?: "",
            input.apiKeyHint ?: "",
        )
        return getApiIntegration(orgId, id)
    }

    /**
     * API 連携の last_sync_at を更新し status を ACTIVE に戻す。
     *
     * @return 更新後の [ApiIntegration]
     */
    @Transactional
    fun syncApiIntegration(orgId: String, id: String): ApiIntegration {
        val now = Timestamp.from(Dates.now())
        jdbc.update(
            """
            UPDATE api_integrations SET last_sync_at = ?, status = 'ACTIVE'
            WHERE id = ? AND org_id = ?
            """.trimIndent(),
            now,
            id,
            orgId,
        )
        return getApiIntegration(orgId, id)
    }

    /**
     * BIM モデル一覧（プロジェクト名 JOIN、projectId 省略可）。
     */
    fun listBimModels(orgId: String, projectId: String?): List<BimModel> {
        val sql = StringBuilder(
            """
            SELECT b.id, b.project_id, p.name AS project_name, b.title, b.format, b.viewer_url,
                   b.file_size_mb, b.status, b.uploaded_by, b.created_at
            FROM bim_models b
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
        sql.append(" ORDER BY b.created_at DESC")
        return jdbc.query(sql.toString(), { rs, _ -> mapBimModel(rs) }, *args.toTypedArray())
    }

    /**
     * BIM モデル 1 件取得。
     *
     * @return 不存在または org 不一致時 null
     */
    fun getBimModel(orgId: String, id: String): BimModel? {
        return try {
            jdbc.queryForObject(
                """
                SELECT b.id, b.project_id, p.name AS project_name, b.title, b.format, b.viewer_url,
                       b.file_size_mb, b.status, b.uploaded_by, b.created_at
                FROM bim_models b
                JOIN construction_projects p ON p.id = b.project_id
                WHERE b.id = ? AND b.org_id = ?
                """.trimIndent(),
                { rs, _ -> mapBimModel(rs) },
                id,
                orgId,
            )
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    /**
     * BIM モデルを INSERT（format 省略時 IFC、viewerUrl 省略時デモ URL）。
     */
    @Transactional
    fun createBimModel(orgId: String, input: CreateBimModelInput): BimModel {
        val id = Ids.random("bim_")
        val format = if (input.format.isNullOrBlank()) "IFC" else input.format
        val viewerUrl = if (input.viewerUrl.isNullOrBlank()) {
            "https://demo.bimdata.io/viewer"
        } else {
            input.viewerUrl
        }
        val projectName = jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            input.projectId,
            orgId,
        )!!
        jdbc.update(
            """
            INSERT INTO bim_models (id, org_id, project_id, title, format, viewer_url, file_size_mb, status, uploaded_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'READY', ?)
            """.trimIndent(),
            id,
            orgId,
            input.projectId,
            input.title,
            format,
            viewerUrl,
            input.fileSizeMb,
            input.uploadedBy ?: "",
        )
        return BimModel(
            id,
            input.projectId,
            projectName,
            input.title,
            format,
            viewerUrl,
            input.fileSizeMb,
            "READY",
            input.uploadedBy ?: "",
            Dates.formatRequired(Dates.now()),
        )
    }

    /** org_id + id で API 連携 1 件取得。 */
    private fun getApiIntegration(orgId: String, id: String): ApiIntegration {
        return jdbc.queryForObject(
            """
            SELECT id, name, provider, endpoint_url, api_key_hint, status, last_sync_at, created_at
            FROM api_integrations WHERE id = ? AND org_id = ?
            """.trimIndent(),
            { rs, _ -> mapApiIntegration(rs) },
            id,
            orgId,
        )!!
    }

    /** ResultSet から [ApiIntegration] をマッピング。 */
    private fun mapApiIntegration(rs: java.sql.ResultSet): ApiIntegration {
        val lastSync = rs.getTimestamp("last_sync_at")
        return ApiIntegration(
            rs.requireStr("id"),
            rs.requireStr("name"),
            rs.requireStr("provider"),
            rs.requireStr("endpoint_url"),
            rs.requireStr("api_key_hint"),
            rs.requireStr("status"),
            if (lastSync == null) "" else Dates.formatRequired(lastSync.toInstant()),
            formatTimestamp(rs.getTimestamp("created_at")),
        )
    }

    /** ResultSet から [BimModel] をマッピング。 */
    private fun mapBimModel(rs: java.sql.ResultSet): BimModel {
        val size = if (rs.getObject("file_size_mb") == null) null else rs.getDouble("file_size_mb")
        return BimModel(
            rs.requireStr("id"),
            rs.requireStr("project_id"),
            rs.requireStr("project_name"),
            rs.requireStr("title"),
            rs.requireStr("format"),
            rs.requireStr("viewer_url"),
            size,
            rs.requireStr("status"),
            rs.requireStr("uploaded_by"),
            formatTimestamp(rs.getTimestamp("created_at")),
        )
    }

    /** 組織全体の月次原価を cost_entries から集計（欠損月 0 埋め）。 */
    private fun orgMonthlyCosts(orgId: String, months: Int): List<MonthlyCostMetric> {
        val since = LocalDate.now().minusMonths((months - 1).toLong()).withDayOfMonth(1)
        val byMonth = HashMap<String, Double>()
        jdbc.query(
            """
            SELECT to_char(date_trunc('month', entry_date), 'YYYY-MM') AS month,
                   COALESCE(SUM(amount), 0) AS amount
            FROM cost_entries
            WHERE org_id = ? AND entry_date >= ?
            GROUP BY 1 ORDER BY 1
            """.trimIndent(),
            { rs ->
                while (rs.next()) {
                    byMonth[rs.requireStr("month")] = rs.getDouble("amount")
                }
            },
            orgId,
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

    /** 単一値 Int クエリ（null 時 0）。 */
    private fun queryInt(sql: String, vararg args: Any): Int {
        val value = jdbc.queryForObject(sql, Int::class.java, *args)
        return value ?: 0
    }

    /** 単一値 Double クエリ（null 時 0.0）。 */
    private fun queryDouble(sql: String, vararg args: Any): Double {
        val value = jdbc.queryForObject(sql, Double::class.java, *args)
        return value ?: 0.0
    }

    /** Timestamp を ISO 形式文字列に変換。 */
    private fun formatTimestamp(ts: Timestamp?): String {
        return if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
    }
}
