package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Date
import java.time.LocalDate
import jp.andpad.api.domain.AttendanceRecord
import jp.andpad.api.domain.Contract
import jp.andpad.api.domain.ContractTemplate
import jp.andpad.api.domain.CrmContact
import jp.andpad.api.domain.CrmInteraction
import jp.andpad.api.domain.DxInitiative
import jp.andpad.api.domain.LeaveRequest
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class SaasRepository(
    private val jdbc: JdbcTemplate,
) {

    fun listOrgModules(orgId: String): List<SaasModule> {
        return jdbc.query(
            """
            SELECT m.code, m.name, m.description, COALESCE(om.enabled, FALSE) AS enabled
            FROM saas_modules m
            LEFT JOIN org_modules om ON om.module_code = m.code AND om.org_id = ?
            ORDER BY m.code
            """.trimIndent(),
            { rs, _ ->
                SaasModule(
                    SaasModuleCode.valueOf(rs.requireStr("code")),
                    rs.requireStr("name"),
                    rs.requireStr("description"),
                    rs.getBoolean("enabled"),
                )
            },
            orgId,
        )
    }

    fun setModuleEnabled(orgId: String, code: SaasModuleCode, enabled: Boolean): SaasModule {
        jdbc.update(
            """
            INSERT INTO org_modules (org_id, module_code, enabled)
            VALUES (?, ?, ?)
            ON CONFLICT (org_id, module_code) DO UPDATE SET enabled = EXCLUDED.enabled
            """.trimIndent(),
            orgId,
            code.name,
            enabled,
        )
        return jdbc.queryForObject(
            """
            SELECT m.code, m.name, m.description, om.enabled
            FROM saas_modules m
            JOIN org_modules om ON om.module_code = m.code AND om.org_id = ?
            WHERE m.code = ?
            """.trimIndent(),
            { rs, _ ->
                SaasModule(
                    SaasModuleCode.valueOf(rs.requireStr("code")),
                    rs.requireStr("name"),
                    rs.requireStr("description"),
                    rs.getBoolean("enabled"),
                )
            },
            orgId,
            code.name,
        )!!
    }

    fun listDxInitiatives(orgId: String): List<DxInitiative> {
        return jdbc.query(
            """
            SELECT i.id, i.title, i.description, i.status, i.progress_pct, i.owner_name,
                   i.due_date, i.created_at,
                   COUNT(t.id)::int AS task_count,
                   COUNT(t.id) FILTER (WHERE t.done)::int AS tasks_done
            FROM dx_initiatives i
            LEFT JOIN dx_tasks t ON t.initiative_id = i.id
            WHERE i.org_id = ?
            GROUP BY i.id
            ORDER BY i.created_at DESC
            """.trimIndent(),
            { rs, _ ->
                DxInitiative(
                    rs.requireStr("id"),
                    rs.requireStr("title"),
                    rs.requireStr("description"),
                    rs.requireStr("status"),
                    rs.getInt("progress_pct"),
                    rs.requireStr("owner_name"),
                    formatDate(rs.getDate("due_date")) ?: "",
                    rs.getInt("task_count"),
                    rs.getInt("tasks_done"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createDxInitiative(
        orgId: String,
        title: String,
        description: String?,
        status: String?,
        progressPct: Int,
        ownerName: String?,
        dueDate: LocalDate?,
    ): DxInitiative {
        val id = Ids.random("dxi_")
        val st = if (status.isNullOrBlank()) "PLANNED" else status
        jdbc.update(
            """
            INSERT INTO dx_initiatives (id, org_id, title, description, status, progress_pct, owner_name, due_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            title,
            description ?: "",
            st,
            progressPct,
            ownerName ?: "",
            if (dueDate == null) null else Date.valueOf(dueDate),
        )
        return DxInitiative(
            id,
            title,
            description ?: "",
            st,
            progressPct,
            ownerName ?: "",
            formatDate(dueDate) ?: "",
            0,
            0,
            Dates.formatRequired(Dates.now()),
        )
    }

    fun listCrmContacts(orgId: String): List<CrmContact> {
        return jdbc.query(
            """
            SELECT id, name, email, phone, company, stage, notes, created_at
            FROM crm_contacts WHERE org_id = ? ORDER BY created_at DESC
            """.trimIndent(),
            { rs, _ ->
                CrmContact(
                    rs.requireStr("id"),
                    rs.requireStr("name"),
                    rs.requireStr("email"),
                    rs.requireStr("phone"),
                    rs.requireStr("company"),
                    rs.requireStr("stage"),
                    rs.requireStr("notes"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createCrmContact(
        orgId: String,
        name: String,
        email: String?,
        phone: String?,
        company: String?,
        stage: String?,
        notes: String?,
    ): CrmContact {
        val id = Ids.random("crm_")
        val st = if (stage.isNullOrBlank()) "LEAD" else stage
        jdbc.update(
            """
            INSERT INTO crm_contacts (id, org_id, name, email, phone, company, stage, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            name,
            email ?: "",
            phone ?: "",
            company ?: "",
            st,
            notes ?: "",
        )
        return CrmContact(
            id,
            name,
            email ?: "",
            phone ?: "",
            company ?: "",
            st,
            notes ?: "",
            Dates.formatRequired(Dates.now()),
        )
    }

    fun listCrmInteractions(orgId: String, contactId: String): List<CrmInteraction> {
        return jdbc.query(
            """
            SELECT id, contact_id, kind, summary, occurred_at
            FROM crm_interactions WHERE org_id = ? AND contact_id = ?
            ORDER BY occurred_at DESC
            """.trimIndent(),
            { rs, _ ->
                CrmInteraction(
                    rs.requireStr("id"),
                    rs.requireStr("contact_id"),
                    rs.requireStr("kind"),
                    rs.requireStr("summary"),
                    formatTimestamp(rs.getTimestamp("occurred_at")) ?: "",
                )
            },
            orgId,
            contactId,
        )
    }

    @Transactional
    fun createCrmInteraction(orgId: String, contactId: String, kind: String?, summary: String): CrmInteraction {
        val id = Ids.random("cri_")
        val k = if (kind.isNullOrBlank()) "NOTE" else kind
        jdbc.update(
            "INSERT INTO crm_interactions (id, org_id, contact_id, kind, summary) VALUES (?, ?, ?, ?, ?)",
            id,
            orgId,
            contactId,
            k,
            summary,
        )
        return CrmInteraction(id, contactId, k, summary, Dates.formatRequired(Dates.now()))
    }

    fun listAttendanceRecords(orgId: String): List<AttendanceRecord> {
        return jdbc.query(
            """
            SELECT a.id, a.user_id, COALESCE(u.name, '') AS user_name, a.clock_in, a.clock_out, a.note
            FROM attendance_records a
            LEFT JOIN users u ON u.id = a.user_id
            WHERE a.org_id = ?
            ORDER BY a.clock_in DESC
            LIMIT 30
            """.trimIndent(),
            { rs, _ ->
                AttendanceRecord(
                    rs.requireStr("id"),
                    rs.requireStr("user_id"),
                    rs.requireStr("user_name"),
                    formatTimestamp(rs.getTimestamp("clock_in")) ?: "",
                    if (rs.getTimestamp("clock_out") == null) {
                        ""
                    } else {
                        formatTimestamp(rs.getTimestamp("clock_out")) ?: ""
                    },
                    rs.requireStr("note"),
                )
            },
            orgId,
        )
    }

    @Transactional
    fun clockIn(orgId: String, userId: String, note: String?): AttendanceRecord {
        val id = Ids.random("att_")
        jdbc.update(
            "INSERT INTO attendance_records (id, org_id, user_id, clock_in, note) VALUES (?, ?, ?, NOW(), ?)",
            id,
            orgId,
            userId,
            note ?: "",
        )
        return findAttendance(orgId, id)
    }

    @Transactional
    fun clockOut(orgId: String, userId: String): AttendanceRecord {
        val recordId = jdbc.queryForObject(
            """
            SELECT id FROM attendance_records
            WHERE org_id = ? AND user_id = ? AND clock_out IS NULL
            ORDER BY clock_in DESC LIMIT 1
            """.trimIndent(),
            String::class.java,
            orgId,
            userId,
        )!!
        jdbc.update(
            "UPDATE attendance_records SET clock_out = NOW() WHERE org_id = ? AND id = ? AND clock_out IS NULL",
            orgId,
            recordId,
        )
        return findAttendance(orgId, recordId)
    }

    fun listLeaveRequests(orgId: String): List<LeaveRequest> {
        return jdbc.query(
            """
            SELECT l.id, l.user_id, COALESCE(u.name, '') AS user_name, l.start_date, l.end_date,
                   l.reason, l.status, l.created_at
            FROM leave_requests l
            LEFT JOIN users u ON u.id = l.user_id
            WHERE l.org_id = ?
            ORDER BY l.created_at DESC
            """.trimIndent(),
            { rs, _ ->
                LeaveRequest(
                    rs.requireStr("id"),
                    rs.requireStr("user_id"),
                    rs.requireStr("user_name"),
                    formatDate(rs.getDate("start_date")) ?: "",
                    formatDate(rs.getDate("end_date")) ?: "",
                    rs.requireStr("reason"),
                    rs.requireStr("status"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createLeaveRequest(
        orgId: String,
        userId: String,
        start: LocalDate,
        end: LocalDate,
        reason: String?,
    ): LeaveRequest {
        val id = Ids.random("lv_")
        jdbc.update(
            """
            INSERT INTO leave_requests (id, org_id, user_id, start_date, end_date, reason)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            userId,
            Date.valueOf(start),
            Date.valueOf(end),
            reason ?: "",
        )
        val userName = jdbc.queryForObject("SELECT name FROM users WHERE id = ?", String::class.java, userId)!!
        return LeaveRequest(
            id,
            userId,
            userName,
            start.toString(),
            end.toString(),
            reason ?: "",
            "PENDING",
            Dates.formatRequired(Dates.now()),
        )
    }

    @Transactional
    fun approveLeaveRequest(orgId: String, id: String): LeaveRequest {
        jdbc.update("UPDATE leave_requests SET status = 'APPROVED' WHERE org_id = ? AND id = ?", orgId, id)
        return findLeaveRequest(orgId, id)
    }

    fun listContractTemplates(orgId: String): List<ContractTemplate> {
        return jdbc.query(
            "SELECT id, name, body, created_at FROM contract_templates WHERE org_id = ? ORDER BY created_at DESC",
            { rs, _ ->
                ContractTemplate(
                    rs.requireStr("id"),
                    rs.requireStr("name"),
                    rs.requireStr("body"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createContractTemplate(orgId: String, name: String, body: String): ContractTemplate {
        val id = Ids.random("ctpl_")
        jdbc.update(
            "INSERT INTO contract_templates (id, org_id, name, body) VALUES (?, ?, ?, ?)",
            id,
            orgId,
            name,
            body,
        )
        return ContractTemplate(id, name, body, Dates.formatRequired(Dates.now()))
    }

    fun listContracts(orgId: String): List<Contract> {
        return jdbc.query(
            """
            SELECT id, COALESCE(template_id, '') AS template_id, title, party_name, party_email,
                   body, status, created_at, signed_at
            FROM contracts WHERE org_id = ? ORDER BY created_at DESC
            """.trimIndent(),
            { rs, _ ->
                Contract(
                    rs.requireStr("id"),
                    emptyToNull(rs.requireStr("template_id")) ?: "",
                    rs.requireStr("title"),
                    rs.requireStr("party_name"),
                    rs.requireStr("party_email"),
                    rs.requireStr("body"),
                    rs.requireStr("status"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                    if (rs.getTimestamp("signed_at") == null) {
                        ""
                    } else {
                        formatTimestamp(rs.getTimestamp("signed_at")) ?: ""
                    },
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createContract(
        orgId: String,
        templateId: String?,
        title: String,
        partyName: String,
        partyEmail: String?,
        body: String?,
    ): Contract {
        val id = Ids.random("ctr_")
        var resolvedBody = body
        if (resolvedBody.isNullOrBlank() && !templateId.isNullOrBlank()) {
            try {
                resolvedBody = jdbc.queryForObject(
                    "SELECT body FROM contract_templates WHERE org_id = ? AND id = ?",
                    String::class.java,
                    orgId,
                    templateId,
                )
            } catch (_: EmptyResultDataAccessException) {
                resolvedBody = ""
            }
        }
        if (resolvedBody == null) {
            resolvedBody = ""
        }
        jdbc.update(
            """
            INSERT INTO contracts (id, org_id, template_id, title, party_name, party_email, body, status)
            VALUES (?, ?, NULLIF(?, ''), ?, ?, ?, ?, 'DRAFT')
            """.trimIndent(),
            id,
            orgId,
            templateId ?: "",
            title,
            partyName,
            partyEmail ?: "",
            resolvedBody,
        )
        return Contract(
            id,
            emptyToNull(templateId) ?: "",
            title,
            partyName,
            partyEmail ?: "",
            resolvedBody,
            "DRAFT",
            Dates.formatRequired(Dates.now()),
            "",
        )
    }

    @Transactional
    fun signContract(orgId: String, id: String): Contract {
        jdbc.update(
            "UPDATE contracts SET status = 'SIGNED', signed_at = NOW() WHERE org_id = ? AND id = ?",
            orgId,
            id,
        )
        return jdbc.queryForObject(
            """
            SELECT id, COALESCE(template_id, '') AS template_id, title, party_name, party_email,
                   body, status, created_at, signed_at
            FROM contracts WHERE org_id = ? AND id = ?
            """.trimIndent(),
            { rs, _ ->
                Contract(
                    rs.requireStr("id"),
                    emptyToNull(rs.requireStr("template_id")) ?: "",
                    rs.requireStr("title"),
                    rs.requireStr("party_name"),
                    rs.requireStr("party_email"),
                    rs.requireStr("body"),
                    rs.requireStr("status"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                    formatTimestamp(rs.getTimestamp("signed_at")) ?: "",
                )
            },
            orgId,
            id,
        )!!
    }

    private fun findAttendance(orgId: String, id: String): AttendanceRecord {
        return jdbc.queryForObject(
            """
            SELECT a.id, a.user_id, COALESCE(u.name, '') AS user_name, a.clock_in, a.clock_out, a.note
            FROM attendance_records a
            LEFT JOIN users u ON u.id = a.user_id
            WHERE a.org_id = ? AND a.id = ?
            """.trimIndent(),
            { rs, _ ->
                AttendanceRecord(
                    rs.requireStr("id"),
                    rs.requireStr("user_id"),
                    rs.requireStr("user_name"),
                    formatTimestamp(rs.getTimestamp("clock_in")) ?: "",
                    if (rs.getTimestamp("clock_out") == null) {
                        ""
                    } else {
                        formatTimestamp(rs.getTimestamp("clock_out")) ?: ""
                    },
                    rs.requireStr("note"),
                )
            },
            orgId,
            id,
        )!!
    }

    private fun findLeaveRequest(orgId: String, id: String): LeaveRequest {
        return jdbc.queryForObject(
            """
            SELECT l.id, l.user_id, COALESCE(u.name, '') AS user_name, l.start_date, l.end_date,
                   l.reason, l.status, l.created_at
            FROM leave_requests l
            LEFT JOIN users u ON u.id = l.user_id
            WHERE l.org_id = ? AND l.id = ?
            """.trimIndent(),
            { rs, _ ->
                LeaveRequest(
                    rs.requireStr("id"),
                    rs.requireStr("user_id"),
                    rs.requireStr("user_name"),
                    formatDate(rs.getDate("start_date")) ?: "",
                    formatDate(rs.getDate("end_date")) ?: "",
                    rs.requireStr("reason"),
                    rs.requireStr("status"),
                    formatTimestamp(rs.getTimestamp("created_at")) ?: "",
                )
            },
            orgId,
            id,
        )!!
    }

    private fun formatTimestamp(ts: java.sql.Timestamp?): String? {
        return if (ts == null) null else Dates.formatRequired(ts.toInstant())
    }

    private fun formatDate(date: Date?): String? {
        return date?.toLocalDate()?.toString()
    }

    private fun formatDate(date: LocalDate?): String? {
        return date?.toString()
    }

    
}
