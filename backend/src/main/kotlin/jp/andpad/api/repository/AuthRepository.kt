package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Timestamp
import java.util.Locale
import java.util.Optional
import jp.andpad.api.domain.MemberRole
import jp.andpad.api.domain.Organization
import jp.andpad.api.domain.PlanTier
import jp.andpad.api.domain.SubscriptionStatus
import jp.andpad.api.domain.User
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class AuthRepository(
    private val jdbc: JdbcTemplate,
    private val passwordEncoder: PasswordEncoder,
) {

    data class LoginResult(val user: User, val organization: Organization, val role: MemberRole)

    data class RegisterInput(
        val clinicName: String,
        val slug: String,
        val ownerName: String,
        val email: String,
        val password: String,
    )

    fun findUserByEmail(email: String): Optional<User> {
        val normalized = email.lowercase(Locale.ROOT).trim()
        return try {
            Optional.of(
                jdbc.queryForObject(
                    """
                    SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url
                    FROM users WHERE LOWER(email) = ?
                    """.trimIndent(),
                    { rs, _ ->
                        User(
                            rs.requireStr("id"),
                            rs.requireStr("email"),
                            rs.requireStr("name"),
                            emptyToNull(rs.requireStr("avatar_url")) ?: "",
                        )
                    },
                    normalized,
                )!!,
            )
        } catch (_: EmptyResultDataAccessException) {
            Optional.empty()
        }
    }

    fun login(email: String, password: String): Optional<LoginResult> {
        val normalized = email.lowercase(Locale.ROOT).trim()
        return try {
            val row = jdbc.queryForMap(
                """
                SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url, password_hash
                FROM users WHERE LOWER(email) = ?
                """.trimIndent(),
                normalized,
            )
            if (!passwordEncoder.matches(password, row["password_hash"] as String)) {
                return Optional.empty()
            }
            val user = User(
                row["id"] as String,
                row["email"] as String,
                row["name"] as String,
                emptyToNull(row["avatar_url"] as String) ?: "",
            )
            val orgRow = jdbc.queryForMap(
                """
                SELECT o.id, o.name, o.slug, o.plan_tier, o.subscription_status, o.seat_count,
                       o.timezone, o.created_at, tm.role
                FROM team_members tm
                JOIN organizations o ON o.id = tm.org_id
                WHERE tm.user_id = ?
                ORDER BY tm.joined_at ASC
                LIMIT 1
                """.trimIndent(),
                user.id,
            )
            jdbc.update(
                "UPDATE team_members SET last_active_at = NOW() WHERE user_id = ? AND org_id = ?",
                user.id,
                orgRow["id"],
            )
            val org = mapOrganization(orgRow, 0)
            val role = MemberRole.valueOf(orgRow["role"] as String)
            Optional.of(LoginResult(user, org, role))
        } catch (_: EmptyResultDataAccessException) {
            Optional.empty()
        }
    }

    @Transactional
    fun register(input: RegisterInput): LoginResult {
        val email = input.email.lowercase(Locale.ROOT).trim()
        val slug = if (input.slug.isNullOrBlank()) {
            "clinic-" + System.currentTimeMillis()
        } else {
            input.slug.lowercase(Locale.ROOT).trim()
        }
        val hash = passwordEncoder.encode(input.password)
        val orgId = Ids.random("org_")
        val userId = Ids.random("user_")
        val tmId = Ids.random("tm_")
        val now = Dates.now()
        jdbc.update(
            """
            INSERT INTO organizations (id, name, slug, plan_tier, subscription_status, seat_count)
            VALUES (?, ?, ?, 'STARTER', 'TRIALING', 5)
            """.trimIndent(),
            orgId,
            input.clinicName,
            slug,
        )
        jdbc.update(
            "INSERT INTO users (id, email, name, password_hash) VALUES (?, ?, ?, ?)",
            userId,
            email,
            input.ownerName,
            hash,
        )
        jdbc.update(
            "INSERT INTO team_members (id, org_id, user_id, role) VALUES (?, ?, ?, 'OWNER')",
            tmId,
            orgId,
            userId,
        )
        jdbc.update("INSERT INTO usage_counters (org_id) VALUES (?)", orgId)
        jdbc.update(
            """
            INSERT INTO org_modules (org_id, module_code, enabled)
            SELECT ?, code, TRUE FROM saas_modules
            """.trimIndent(),
            orgId,
        )
        val user = User(userId, email, input.ownerName, "")
        val org = Organization(
            orgId,
            input.clinicName,
            slug,
            PlanTier.STARTER,
            SubscriptionStatus.TRIALING,
            5,
            "Asia/Tokyo",
            1,
            Dates.formatRequired(now),
            emptyList(),
        )
        return LoginResult(user, org, MemberRole.OWNER)
    }

    fun sessionByUser(userId: String, orgId: String): Optional<LoginResult> {
        return try {
            val user = jdbc.queryForObject(
                """
                SELECT id, email, name, COALESCE(avatar_url, '') AS avatar_url
                FROM users WHERE id = ?
                """.trimIndent(),
                { rs, _ ->
                    User(
                        rs.requireStr("id"),
                        rs.requireStr("email"),
                        rs.requireStr("name"),
                        emptyToNull(rs.requireStr("avatar_url")) ?: "",
                    )
                },
                userId,
            )!!
            val orgRow = jdbc.queryForMap(
                """
                SELECT o.id, o.name, o.slug, o.plan_tier, o.subscription_status, o.seat_count,
                       o.timezone, o.created_at, tm.role
                FROM organizations o
                JOIN team_members tm ON tm.org_id = o.id
                WHERE o.id = ? AND tm.user_id = ?
                """.trimIndent(),
                orgId,
                userId,
            )
            val role = MemberRole.valueOf(orgRow["role"] as String)
            val memberCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM team_members WHERE org_id = ?",
                Int::class.java,
                orgId,
            )!!
            val org = mapOrganization(orgRow, memberCount)
            Optional.of(LoginResult(user, org, role))
        } catch (_: EmptyResultDataAccessException) {
            Optional.empty()
        }
    }

    private fun mapOrganization(row: Map<String, Any?>, memberCount: Int): Organization {
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
            emptyList(),
        )
    }

    
}
