package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Array
import java.sql.Timestamp
import jp.andpad.api.domain.ExtendedTypes.ConsultMessage
import jp.andpad.api.domain.ExtendedTypes.ConsultThread
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import jp.andpad.api.util.RagHelper
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ConsultRagRepository(
    private val jdbc: JdbcTemplate,
) {

    fun listThreads(orgId: String, userId: String, orgWide: Boolean): List<ConsultThread> {
        val sql = if (orgWide) {
            """
            SELECT id, title, created_at FROM consultation_threads
            WHERE org_id = ? ORDER BY created_at DESC
            """.trimIndent()
        } else {
            """
            SELECT id, title, created_at FROM consultation_threads
            WHERE org_id = ? AND user_id = ? ORDER BY created_at DESC
            """.trimIndent()
        }
        return if (orgWide) {
            jdbc.query(
                sql,
                { rs, _ ->
                    ConsultThread(
                        rs.requireStr("id"),
                        rs.requireStr("title"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                        emptyList(),
                    )
                },
                orgId,
            )
        } else {
            jdbc.query(
                sql,
                { rs, _ ->
                    ConsultThread(
                        rs.requireStr("id"),
                        rs.requireStr("title"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                        emptyList(),
                    )
                },
                orgId,
                userId,
            )
        }
    }

    fun getThread(orgId: String, userId: String, threadId: String, orgWide: Boolean): ConsultThread? {
        return try {
            val thread = if (orgWide) {
                jdbc.queryForObject(
                    """
                    SELECT id, title, created_at FROM consultation_threads
                    WHERE org_id = ? AND id = ?
                    """.trimIndent(),
                    { rs, _ ->
                        ConsultThread(
                            rs.requireStr("id"),
                            rs.requireStr("title"),
                            formatTimestamp(rs.getTimestamp("created_at")),
                            emptyList(),
                        )
                    },
                    orgId,
                    threadId,
                )!!
            } else {
                jdbc.queryForObject(
                    """
                    SELECT id, title, created_at FROM consultation_threads
                    WHERE org_id = ? AND user_id = ? AND id = ?
                    """.trimIndent(),
                    { rs, _ ->
                        ConsultThread(
                            rs.requireStr("id"),
                            rs.requireStr("title"),
                            formatTimestamp(rs.getTimestamp("created_at")),
                            emptyList(),
                        )
                    },
                    orgId,
                    userId,
                    threadId,
                )!!
            }
            val messages = listMessages(orgId, threadId)
            ConsultThread(thread.id, thread.title, thread.createdAt, messages)
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    fun listMessages(orgId: String, threadId: String): List<ConsultMessage> {
        return jdbc.query(
            """
            SELECT id, role, content, created_at FROM consultation_messages
            WHERE org_id = ? AND thread_id = ? ORDER BY created_at ASC
            """.trimIndent(),
            { rs, _ ->
                ConsultMessage(
                    rs.requireStr("id"),
                    rs.requireStr("role"),
                    rs.requireStr("content"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            orgId,
            threadId,
        )
    }

    @Transactional
    fun createThread(orgId: String, userId: String, title: String): ConsultThread {
        val id = Ids.random("ct_")
        jdbc.update(
            """
            INSERT INTO consultation_threads (id, org_id, user_id, title) VALUES (?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            userId,
            title,
        )
        return ConsultThread(id, title, Dates.formatRequired(Dates.now()), emptyList())
    }

    fun verifyThreadAccess(orgId: String, userId: String, threadId: String, orgWide: Boolean): Boolean {
        val count = if (orgWide) {
            jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM consultation_threads
                WHERE id = ? AND org_id = ?
                """.trimIndent(),
                Int::class.java,
                threadId,
                orgId,
            )
        } else {
            jdbc.queryForObject(
                """
                SELECT COUNT(*) FROM consultation_threads
                WHERE id = ? AND org_id = ? AND user_id = ?
                """.trimIndent(),
                Int::class.java,
                threadId,
                orgId,
                userId,
            )
        }
        return count != null && count > 0
    }

    @Transactional
    fun addMessage(orgId: String, threadId: String, role: String, content: String): ConsultMessage {
        val id = Ids.random("cm_")
        val now = Timestamp.from(Dates.now())
        jdbc.update(
            """
            INSERT INTO consultation_messages (id, org_id, thread_id, role, content, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            threadId,
            role,
            content,
            now,
        )
        return ConsultMessage(id, role, content, Dates.formatRequired(now.toInstant()))
    }

    @Transactional
    fun incrementConsultUsage(orgId: String, tokens: Int) {
        jdbc.update(
            """
            INSERT INTO usage_counters (org_id, consult_tokens_month) VALUES (?, ?)
            ON CONFLICT (org_id) DO UPDATE SET
                consult_tokens_month = usage_counters.consult_tokens_month + EXCLUDED.consult_tokens_month
            """.trimIndent(),
            orgId,
            tokens,
        )
    }

    fun listRagDocuments(orgId: String): List<RagDocument> {
        return jdbc.query(
            """
            SELECT id, title, content, tags, created_at FROM rag_documents
            WHERE org_id = ? ORDER BY created_at DESC
            """.trimIndent(),
            { rs, _ ->
                RagDocument(
                    rs.requireStr("id"),
                    rs.requireStr("title"),
                    rs.requireStr("content"),
                    readStringTags(rs),
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            orgId,
        )
    }

    @Transactional
    fun createRagDocument(orgId: String, input: CreateRagDocumentInput): RagDocument {
        val id = Ids.random("rag_")
        val tags = input.tags ?: emptyList()
        val now = Timestamp.from(Dates.now())
        jdbc.update { connection ->
            val ps = connection.prepareStatement(
                """
                INSERT INTO rag_documents (id, org_id, title, content, tags, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
            )
            ps.setString(1, id)
            ps.setString(2, orgId)
            ps.setString(3, input.title)
            ps.setString(4, input.content)
            ps.setArray(5, connection.createArrayOf("text", tags.toTypedArray()))
            ps.setTimestamp(6, now)
            ps
        }
        return RagDocument(id, input.title, input.content, tags, Dates.formatRequired(now.toInstant()))
    }

    fun searchRagDocuments(orgId: String, query: String?, limit: Int): List<RagSearchHit> {
        val lim = if (limit <= 0) 5 else limit
        val q = query?.trim() ?: ""
        if (q.isEmpty()) {
            return emptyList()
        }
        val hits = jdbc.query(
            """
            SELECT id, title, content,
                   ts_rank(
                       to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '')),
                       plainto_tsquery('simple', ?)
                   ) AS score
            FROM rag_documents
            WHERE org_id = ?
              AND to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, ''))
                  @@ plainto_tsquery('simple', ?)
            ORDER BY score DESC
            LIMIT ?
            """.trimIndent(),
            { rs, _ ->
                RagSearchHit(
                    rs.requireStr("id"),
                    rs.requireStr("title"),
                    RagHelper.snippet(rs.requireStr("content"), q, 180),
                    rs.getDouble("score"),
                )
            },
            q,
            orgId,
            q,
            lim,
        )
        if (hits.isNotEmpty()) {
            return hits
        }
        val like = "%$q%"
        return jdbc.query(
            """
            SELECT id, title, content FROM rag_documents
            WHERE org_id = ? AND (title ILIKE ? OR content ILIKE ?)
            ORDER BY created_at DESC LIMIT ?
            """.trimIndent(),
            { rs, _ ->
                RagSearchHit(
                    rs.requireStr("id"),
                    rs.requireStr("title"),
                    RagHelper.snippet(rs.requireStr("content"), q, 180),
                    0.6,
                )
            },
            orgId,
            like,
            like,
            lim,
        )
    }

    private fun formatTimestamp(ts: Timestamp?): String =
        if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
}
