package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Array
import java.sql.Timestamp
import jp.andpad.api.domain.LearningTypes.DashboardStats
import jp.andpad.api.domain.LearningTypes.Instructor
import jp.andpad.api.domain.LearningTypes.LearningPath
import jp.andpad.api.domain.LearningTypes.PageInfo
import jp.andpad.api.domain.LearningTypes.Video
import jp.andpad.api.domain.LearningTypes.VideoPage
import jp.andpad.api.domain.LearningTypes.WatchProgress
import jp.andpad.api.domain.SkillLevel
import jp.andpad.api.domain.VideoCategory
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class LearningRepository(
    private val jdbc: JdbcTemplate,
) {

    fun dashboard(orgId: String): DashboardStats {
        val videosTotal = queryInt("SELECT COUNT(*) FROM videos WHERE org_id = ?", orgId)
        val pathsTotal = queryInt("SELECT COUNT(*) FROM learning_paths WHERE org_id = ?", orgId)
        val quizzesTotal = queryInt("SELECT COUNT(*) FROM quizzes WHERE org_id = ?", orgId)
        val progress = jdbc.queryForObject(
            """
            SELECT COUNT(*) FILTER (WHERE completed), COALESCE(SUM(position_sec), 0)
            FROM watch_progress WHERE org_id = ?
            """.trimIndent(),
            { rs, _ -> arrayOf(rs.getInt(1), rs.getInt(2)) },
            orgId,
        )
        val completions = progress?.get(0) ?: 0
        val watchHours = if (progress == null) 0.0 else progress[1] / 3600.0
        val activeLearners = queryInt(
            "SELECT COUNT(DISTINCT learner_id) FROM watch_progress WHERE org_id = ?",
            orgId,
        )
        return DashboardStats(videosTotal, pathsTotal, quizzesTotal, completions, watchHours, activeLearners)
    }

    fun paginateVideos(
        orgId: String,
        category: VideoCategory?,
        skillLevel: SkillLevel?,
        search: String?,
        page: Int,
        pageSize: Int,
    ): VideoPage {
        val p = if (page < 1) 1 else page
        val size = if (pageSize < 1) 12 else pageSize
        val where = StringBuilder("WHERE v.org_id = ?")
        val args = ArrayList<Any>()
        args.add(orgId)
        if (category != null) {
            where.append(" AND v.category = ?")
            args.add(category.name)
        }
        if (skillLevel != null) {
            where.append(" AND v.skill_level = ?")
            args.add(skillLevel.name)
        }
        if (!search.isNullOrBlank()) {
            where.append(" AND (LOWER(v.title) LIKE ? OR LOWER(v.description) LIKE ?)")
            val pattern = "%" + search.trim().lowercase() + "%"
            args.add(pattern)
            args.add(pattern)
        }
        val total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM videos v $where",
            Int::class.java,
            *args.toTypedArray(),
        )!!
        val totalPages = maxOf(1, kotlin.math.ceil(total.toDouble() / size).toInt())
        val offset = (p - 1) * size
        args.add(size)
        args.add(offset)
        val items = jdbc.query(
            """
            SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                   v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                   v.view_count, v.featured, v.published_at
            FROM videos v
            LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
            """.trimIndent() + where + " ORDER BY v.published_at DESC LIMIT ? OFFSET ?",
            { rs, _ -> mapVideo(rs) },
            *args.toTypedArray(),
        )
        return VideoPage(items, PageInfo(total, p, size, totalPages))
    }

    fun getVideo(orgId: String, id: String): Video? {
        return try {
            jdbc.queryForObject(
                """
                SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                       v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                       v.view_count, v.featured, v.published_at
                FROM videos v
                LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
                WHERE v.org_id = ? AND v.id = ?
                """.trimIndent(),
                { rs, _ -> mapVideo(rs) },
                orgId,
                id,
            )
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    fun featuredVideos(orgId: String): List<Video> {
        return jdbc.query(
            """
            SELECT v.id, v.title, v.description, v.category, v.procedure, v.skill_level, v.duration_sec,
                   v.thumbnail_url, v.video_url, v.instructor_id, COALESCE(i.name, '') AS instructor_name, v.tags,
                   v.view_count, v.featured, v.published_at
            FROM videos v
            LEFT JOIN instructors i ON i.id = v.instructor_id AND i.org_id = v.org_id
            WHERE v.org_id = ? AND v.featured = TRUE
            ORDER BY v.view_count DESC LIMIT 12
            """.trimIndent(),
            { rs, _ -> mapVideo(rs) },
            orgId,
        )
    }

    @Transactional
    fun incrementViewCount(orgId: String, id: String): Video? {
        val updated = jdbc.update(
            "UPDATE videos SET view_count = view_count + 1 WHERE org_id = ? AND id = ?",
            orgId,
            id,
        )
        if (updated == 0) {
            return null
        }
        return getVideo(orgId, id)
    }

    fun listInstructors(orgId: String): List<Instructor> {
        return jdbc.query(
            """
            SELECT i.id, i.name, i.title, i.specialty, i.bio, i.avatar_url,
                   (SELECT COUNT(*) FROM videos v WHERE v.instructor_id = i.id AND v.org_id = ?)::int AS video_count
            FROM instructors i WHERE i.org_id = ? ORDER BY i.name
            """.trimIndent(),
            { rs, _ ->
                Instructor(
                    rs.requireStr("id"),
                    rs.requireStr("name"),
                    rs.requireStr("title"),
                    rs.requireStr("specialty"),
                    rs.requireStr("bio"),
                    rs.requireStr("avatar_url"),
                    rs.getInt("video_count"),
                )
            },
            orgId,
            orgId,
        )
    }

    fun getInstructor(orgId: String, id: String): Instructor? {
        return try {
            jdbc.queryForObject(
                """
                SELECT i.id, i.name, i.title, i.specialty, i.bio, i.avatar_url,
                       (SELECT COUNT(*) FROM videos v WHERE v.instructor_id = i.id AND v.org_id = ?)::int AS video_count
                FROM instructors i WHERE i.org_id = ? AND i.id = ?
                """.trimIndent(),
                { rs, _ ->
                    Instructor(
                        rs.requireStr("id"),
                        rs.requireStr("name"),
                        rs.requireStr("title"),
                        rs.requireStr("specialty"),
                        rs.requireStr("bio"),
                        rs.requireStr("avatar_url"),
                        rs.getInt("video_count"),
                    )
                },
                orgId,
                orgId,
                id,
            )
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    fun listPaths(orgId: String, category: VideoCategory?, skillLevel: SkillLevel?): List<LearningPath> {
        val sql = StringBuilder(
            """
            SELECT id, title, description, category, skill_level, estimated_minutes, enrolled_count, certificate_title
            FROM learning_paths WHERE org_id = ?
            """.trimIndent(),
        )
        val args = ArrayList<Any>()
        args.add(orgId)
        if (category != null) {
            sql.append(" AND category = ?")
            args.add(category.name)
        }
        if (skillLevel != null) {
            sql.append(" AND skill_level = ?")
            args.add(skillLevel.name)
        }
        return jdbc.query(
            sql.toString(),
            { rs, _ -> mapPath(rs, pathVideoIds(rs.requireStr("id"))) },
            *args.toTypedArray(),
        )
    }

    fun getPath(orgId: String, id: String): LearningPath? {
        return try {
            jdbc.queryForObject(
                """
                SELECT id, title, description, category, skill_level, estimated_minutes, enrolled_count, certificate_title
                FROM learning_paths WHERE org_id = ? AND id = ?
                """.trimIndent(),
                { rs, _ -> mapPath(rs, pathVideoIds(id)) },
                orgId,
                id,
            )
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    @Transactional
    fun updateProgress(orgId: String, progress: WatchProgress): WatchProgress {
        val id = if (progress.id.isBlank()) Ids.random("wp_") else progress.id
        val now = Timestamp.from(Dates.now())
        jdbc.update(
            """
            INSERT INTO watch_progress (id, org_id, video_id, learner_id, position_sec, completed, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (org_id, video_id, learner_id) DO UPDATE SET
                position_sec = EXCLUDED.position_sec,
                completed = EXCLUDED.completed,
                updated_at = EXCLUDED.updated_at
            """.trimIndent(),
            id,
            orgId,
            progress.videoId,
            progress.learnerId,
            progress.positionSec,
            progress.completed,
            now,
        )
        return WatchProgress(
            id,
            progress.videoId,
            progress.learnerId,
            progress.positionSec,
            progress.completed,
            Dates.formatRequired(now.toInstant()),
        )
    }

    private fun pathVideoIds(pathId: String): List<String> {
        return jdbc.query(
            "SELECT video_id FROM path_videos WHERE path_id = ? ORDER BY sort_order",
            { rs, _ -> rs.requireStr("video_id") },
            pathId,
        )
    }

    private fun mapPath(rs: java.sql.ResultSet, videoIds: List<String>): LearningPath {
        return LearningPath(
            rs.requireStr("id"),
            rs.requireStr("title"),
            rs.requireStr("description"),
            VideoCategory.valueOf(rs.requireStr("category")),
            SkillLevel.valueOf(rs.requireStr("skill_level")),
            videoIds,
            rs.getInt("estimated_minutes"),
            rs.getInt("enrolled_count"),
            rs.requireStr("certificate_title"),
        )
    }

    private fun mapVideo(rs: java.sql.ResultSet): Video {
        val published = rs.getTimestamp("published_at")
        return Video(
            rs.requireStr("id"),
            rs.requireStr("title"),
            rs.requireStr("description"),
            VideoCategory.valueOf(rs.requireStr("category")),
            rs.requireStr("procedure"),
            SkillLevel.valueOf(rs.requireStr("skill_level")),
            rs.getInt("duration_sec"),
            rs.requireStr("thumbnail_url"),
            rs.requireStr("video_url"),
            rs.requireStr("instructor_id"),
            rs.requireStr("instructor_name"),
            readStringTags(rs),
            rs.getInt("view_count"),
            if (published == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(published.toInstant()),
            rs.getBoolean("featured"),
        )
    }

    private fun queryInt(sql: String, vararg args: Any): Int {
        val value = jdbc.queryForObject(sql, Int::class.java, *args)
        return value ?: 0
    }
}
