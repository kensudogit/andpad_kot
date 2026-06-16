package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Timestamp
import jp.andpad.api.domain.MemberRole
import jp.andpad.api.domain.Organization
import jp.andpad.api.domain.PlanTier
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SubscriptionStatus
import jp.andpad.api.domain.TeamMember
import jp.andpad.api.domain.UsageSummary
import jp.andpad.api.domain.User
import jp.andpad.api.util.Dates
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

/**
 * 組織・チーム・利用状況の JDBC リポジトリ。
 *
 * **責務**: organizations マスタ取得／更新、team_members 一覧、usage_counters 集計。
 *
 * **参照テーブル**: [organizations], [team_members], [users], [construction_projects], [usage_counters]
 *
 * **テナント分離**: org_id を主キーとして organizations / team_members / usage_counters をスコープ。
 * getOrganization は id = orgId のみ（呼び出し元が TenantContext から orgId を渡す前提）。
 */
@Repository
class OrganizationRepository(
    private val jdbc: JdbcTemplate,
) {

    /**
     * 組織情報を取得（メンバー数・有効モジュールは引数で付与）。
     *
     * @param orgId テナント ID
     * @param enabledModules サービス層で取得済みのモジュール一覧
     */
    fun getOrganization(orgId: String, enabledModules: List<SaasModule>): Organization {
        val row = jdbc.queryForMap(
            // organizations マスタ 1 件
            """
            SELECT id, name, slug, plan_tier, subscription_status, seat_count, timezone, created_at
            FROM organizations WHERE id = ?
            """.trimIndent(),
            orgId,
        )
        val memberCount = jdbc.queryForObject(
            "SELECT COUNT(*) FROM team_members WHERE org_id = ?",
            Int::class.java,
            orgId,
        )!!
        return mapOrganization(row, memberCount, enabledModules)
    }

    /**
     * 組織属性を部分更新（patch に含まれるキーのみ上書き）。
     *
     * @param patch name / slug / seatCount / timezone のいずれか
     */
    fun updateOrganization(
        orgId: String,
        patch: Map<String, Any>,
        enabledModules: List<SaasModule>,
    ): Organization {
        val current = getOrganization(orgId, enabledModules)
        val name = if (patch.containsKey("name")) patch["name"] as String else current.name
        val slug = if (patch.containsKey("slug")) patch["slug"] as String else current.slug
        val seatCount = if (patch.containsKey("seatCount")) patch["seatCount"] as Int else current.seatCount
        val timezone = if (patch.containsKey("timezone")) patch["timezone"] as String else current.timezone
        jdbc.update(
            "UPDATE organizations SET name = ?, slug = ?, seat_count = ?, timezone = ? WHERE id = ?",
            name,
            slug,
            seatCount,
            timezone,
            orgId,
        )
        return getOrganization(orgId, enabledModules)
    }

    /** 組織メンバー一覧（users JOIN、org_id スコープ）。 */
    fun listTeamMembers(orgId: String): List<TeamMember> {
        return jdbc.query(
            """
            SELECT tm.id, tm.role, tm.joined_at, tm.last_active_at,
                   u.id AS user_id, u.email, u.name, COALESCE(u.avatar_url, '') AS avatar_url
            FROM team_members tm
            JOIN users u ON u.id = tm.user_id
            WHERE tm.org_id = ?
            """.trimIndent(),
            { rs, _ ->
                val user = User(
                    rs.requireStr("user_id"),
                    rs.requireStr("email"),
                    rs.requireStr("name"),
                    emptyToNull(rs.requireStr("avatar_url")) ?: "",
                )
                TeamMember(
                    rs.requireStr("id"),
                    user,
                    MemberRole.valueOf(rs.requireStr("role")),
                    formatTimestamp(rs.getTimestamp("joined_at")),
                    formatTimestamp(rs.getTimestamp("last_active_at")),
                )
            },
            orgId,
        )
    }

    /**
     * 利用状況サマリ（メンバー数・プロジェクト数・月次 API/相談トークン）。
     *
     * usage_counters 行不存在時は apiCalls / consultTokens = 0。
     * 上限値（10, 50, 10000）は固定ハードコード。
     */
    fun usageSummary(orgId: String): UsageSummary {
        val members = jdbc.queryForObject(
            "SELECT COUNT(*) FROM team_members WHERE org_id = ?",
            Int::class.java,
            orgId,
        )!!
        val projects = jdbc.queryForObject(
            "SELECT COUNT(*) FROM construction_projects WHERE org_id = ?",
            Int::class.java,
            orgId,
        )!!
        var apiCalls = 0
        var consultTokens = 0
        try {
            val row = jdbc.queryForMap(
                """
                SELECT COALESCE(api_calls_month, 0) AS api_calls_month,
                       COALESCE(consult_tokens_month, 0) AS consult_tokens_month
                FROM usage_counters WHERE org_id = ?
                """.trimIndent(),
                orgId,
            )
            apiCalls = (row["api_calls_month"] as Number).toInt()
            consultTokens = (row["consult_tokens_month"] as Number).toInt()
        } catch (_: EmptyResultDataAccessException) {
        }
        return UsageSummary(members, 10, projects, 50, apiCalls, 10000, consultTokens)
    }

    /** 組織 ID の存在確認（EXISTS サブクエリ）。 */
    fun orgExists(orgId: String): Boolean {
        val exists = jdbc.queryForObject(
            "SELECT EXISTS(SELECT 1 FROM organizations WHERE id = ?)",
            Boolean::class.java,
            orgId,
        )
        return exists == true
    }

    /** Map 行を [Organization] ドメインに変換。 */
    private fun mapOrganization(
        row: Map<String, Any?>,
        memberCount: Int,
        modules: List<SaasModule>,
    ): Organization {
        val createdAt = when (val created = row["created_at"]) {
            is Timestamp -> created.toInstant()
            else -> Dates.now()
        }
        return Organization(
            row["id"] as String,
            row["name"] as String,
            row["slug"] as String,
            PlanTier.valueOf(row["plan_tier"] as String),
            SubscriptionStatus.valueOf(row["subscription_status"] as String),
            (row["seat_count"] as Number).toInt(),
            row["timezone"] as String,
            memberCount,
            Dates.formatRequired(createdAt),
            modules,
        )
    }

    /** Timestamp を ISO 形式文字列に変換。 */
    private fun formatTimestamp(ts: Timestamp?): String {
        return if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
    }

    
}
