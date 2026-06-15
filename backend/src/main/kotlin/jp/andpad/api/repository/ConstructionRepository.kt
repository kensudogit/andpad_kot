package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Date
import java.time.LocalDate
import jp.andpad.api.domain.ConstructionProject
import jp.andpad.api.domain.ConstructionProjectStatus
import jp.andpad.api.domain.ProjectModuleRecord
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ConstructionRepository(
    private val jdbc: JdbcTemplate,
) {

    fun listProjects(orgId: String): List<ConstructionProject> {
        return jdbc.query(
            """
            SELECT p.id, p.name, p.site_address, p.status, p.manager_name,
                   p.start_date, p.end_date, p.created_at, COUNT(r.id)::int AS record_count
            FROM construction_projects p
            LEFT JOIN project_module_records r ON r.project_id = p.id
            WHERE p.org_id = ?
            GROUP BY p.id
            ORDER BY p.created_at DESC
            """.trimIndent(),
            { rs, _ ->
                ConstructionProject(
                    rs.requireStr("id"),
                    rs.requireStr("name"),
                    rs.requireStr("site_address"),
                    ConstructionProjectStatus.valueOf(rs.requireStr("status")),
                    rs.requireStr("manager_name"),
                    formatDate(rs.getDate("start_date")) ?: "",
                    formatDate(rs.getDate("end_date")) ?: "",
                    rs.getInt("record_count"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createProject(
        orgId: String,
        name: String,
        siteAddress: String?,
        status: ConstructionProjectStatus?,
        managerName: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): ConstructionProject {
        val id = Ids.random("prj_")
        val st = status ?: ConstructionProjectStatus.PLANNING
        jdbc.update(
            """
            INSERT INTO construction_projects (id, org_id, name, site_address, status, manager_name, start_date, end_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            name,
            siteAddress ?: "",
            st.name,
            managerName ?: "",
            if (startDate == null) null else Date.valueOf(startDate),
            if (endDate == null) null else Date.valueOf(endDate),
        )
        return ConstructionProject(
            id,
            name,
            siteAddress ?: "",
            st,
            managerName ?: "",
            formatDate(startDate) ?: "",
            formatDate(endDate) ?: "",
            0,
            Dates.formatRequired(Dates.now()),
        )
    }

    fun listModuleRecords(orgId: String, code: SaasModuleCode?, projectId: String?): List<ProjectModuleRecord> {
        if (code == null) {
            return emptyList()
        }
        val sql = StringBuilder(
            """
            SELECT r.id, r.project_id, p.name AS project_name, r.module_code, r.title, r.status,
                   r.detail, r.amount, r.person_name, r.record_date, r.created_at
            FROM project_module_records r
            JOIN construction_projects p ON p.id = r.project_id
            WHERE r.org_id = ? AND r.module_code = ?
            """.trimIndent(),
        )
        val args = ArrayList<Any>()
        args.add(orgId)
        args.add(code.name)
        if (!projectId.isNullOrBlank()) {
            sql.append(" AND r.project_id = ?")
            args.add(projectId)
        }
        sql.append(" ORDER BY r.created_at DESC")
        return jdbc.query(
            sql.toString(),
            { rs, _ ->
                ProjectModuleRecord(
                    rs.requireStr("id"),
                    rs.requireStr("project_id"),
                    rs.requireStr("project_name"),
                    SaasModuleCode.valueOf(rs.requireStr("module_code")),
                    rs.requireStr("title"),
                    rs.requireStr("status"),
                    rs.requireStr("detail"),
                    if (rs.getObject("amount") == null) null else rs.getDouble("amount"),
                    rs.requireStr("person_name"),
                    formatDate(rs.getDate("record_date")) ?: "",
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            *args.toTypedArray(),
        )
    }

    @Transactional
    fun createModuleRecord(
        orgId: String,
        projectId: String,
        moduleCode: SaasModuleCode,
        title: String,
        status: String?,
        detail: String?,
        amount: Double?,
        personName: String?,
        recordDate: LocalDate?,
    ): ProjectModuleRecord {
        val id = Ids.random("rec_")
        val st = if (status.isNullOrBlank()) "OPEN" else status
        val projectName = jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            projectId,
            orgId,
        )!!
        jdbc.update(
            """
            INSERT INTO project_module_records (id, org_id, project_id, module_code, title, status, detail,
                amount, person_name, record_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            projectId,
            moduleCode.name,
            title,
            st,
            detail ?: "",
            amount,
            personName ?: "",
            if (recordDate == null) null else Date.valueOf(recordDate),
        )
        return ProjectModuleRecord(
            id,
            projectId,
            projectName,
            moduleCode,
            title,
            st,
            detail ?: "",
            amount,
            personName ?: "",
            formatDate(recordDate) ?: "",
            Dates.formatRequired(Dates.now()),
        )
    }

    fun projectName(orgId: String, projectId: String): String {
        return jdbc.queryForObject(
            "SELECT name FROM construction_projects WHERE id = ? AND org_id = ?",
            String::class.java,
            projectId,
            orgId,
        )!!
    }

    private fun formatTimestamp(ts: java.sql.Timestamp?): String {
        return if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
    }

    private fun formatDate(date: Date?): String? {
        return date?.toLocalDate()?.toString()
    }

    private fun formatDate(date: LocalDate?): String? {
        return date?.toString()
    }
}
