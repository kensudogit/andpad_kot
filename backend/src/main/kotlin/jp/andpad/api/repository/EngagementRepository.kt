package jp.andpad.api.repository

import jp.andpad.api.util.emptyToNull
import jp.andpad.api.util.readStringTags
import jp.andpad.api.util.requireStr

import java.sql.Timestamp
import jp.andpad.api.domain.LearningTypes.Bookmark
import jp.andpad.api.domain.LearningTypes.Certificate
import jp.andpad.api.domain.LearningTypes.Quiz
import jp.andpad.api.domain.LearningTypes.QuizAttempt
import jp.andpad.api.domain.LearningTypes.QuizChoice
import jp.andpad.api.domain.LearningTypes.QuizQuestion
import jp.andpad.api.domain.LearningTypes.VideoNote
import jp.andpad.api.domain.LearningTypes.WatchProgress
import jp.andpad.api.util.Dates
import jp.andpad.api.util.Ids
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * ラーニングエンゲージメント（進捗・ノート・ブックマーク・クイズ）の JDBC リポジトリ。
 *
 * **責務**: 視聴進捗・ノート・ブックマーク、クイズ出題／採点、修了証、パス登録。
 *
 * **参照テーブル**: [watch_progress], [video_notes], [bookmarks], [quizzes],
 * [quiz_questions], [quiz_choices], [quiz_attempts], [certificates], [enrollments], [learning_paths]
 *
 * **テナント分離**: 全操作で `org_id = ?` を必須とする。
 * quiz_questions / quiz_choices は quiz_id 経由（quizzes が org スコープ）。
 */
@Repository
class EngagementRepository(
    private val jdbc: JdbcTemplate,
) {

    /** 学習者の視聴進捗一覧（更新日降順）。 */
    fun listProgress(orgId: String, learnerId: String): List<WatchProgress> {
        return jdbc.query(
            """
            SELECT id, video_id, learner_id, position_sec, completed, updated_at
            FROM watch_progress WHERE org_id = ? AND learner_id = ?
            ORDER BY updated_at DESC
            """.trimIndent(),
            { rs, _ ->
                WatchProgress(
                    rs.requireStr("id"),
                    rs.requireStr("video_id"),
                    rs.requireStr("learner_id"),
                    rs.getInt("position_sec"),
                    rs.getBoolean("completed"),
                    formatTimestamp(rs.getTimestamp("updated_at")),
                )
            },
            orgId,
            learnerId,
        )
    }

    /** 動画ノート一覧（タイムスタンプ昇順）。 */
    fun listNotes(orgId: String, videoId: String, learnerId: String): List<VideoNote> {
        return jdbc.query(
            """
            SELECT id, video_id, learner_id, timestamp_sec, body, created_at
            FROM video_notes
            WHERE org_id = ? AND video_id = ? AND learner_id = ?
            ORDER BY timestamp_sec
            """.trimIndent(),
            { rs, _ ->
                VideoNote(
                    rs.requireStr("id"),
                    rs.requireStr("video_id"),
                    rs.requireStr("learner_id"),
                    rs.getInt("timestamp_sec"),
                    rs.requireStr("body"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            orgId,
            videoId,
            learnerId,
        )
    }

    /** 動画ノート INSERT（id 空文字時は新規生成）。 */
    @Transactional
    fun createNote(orgId: String, note: VideoNote): VideoNote {
        val id = if (note.id.isBlank()) Ids.random("note_") else note.id
        val now = Timestamp.from(Dates.now())
        jdbc.update(
            """
            INSERT INTO video_notes (id, org_id, video_id, learner_id, timestamp_sec, body, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            note.videoId,
            note.learnerId,
            note.timestampSec,
            note.body,
            now,
        )
        return VideoNote(id, note.videoId, note.learnerId, note.timestampSec, note.body, Dates.formatRequired(now.toInstant()))
    }

    /**
     * 動画ノート DELETE。
     *
     * @return 1 件以上削除時 true
     */
    @Transactional
    fun deleteNote(orgId: String, id: String): Boolean {
        return jdbc.update("DELETE FROM video_notes WHERE org_id = ? AND id = ?", orgId, id) > 0
    }

    /** ブックマーク一覧（作成日降順）。 */
    fun listBookmarks(orgId: String, learnerId: String): List<Bookmark> {
        return jdbc.query(
            """
            SELECT id, video_id, learner_id, created_at FROM bookmarks
            WHERE org_id = ? AND learner_id = ? ORDER BY created_at DESC
            """.trimIndent(),
            { rs, _ ->
                Bookmark(
                    rs.requireStr("id"),
                    rs.requireStr("video_id"),
                    rs.requireStr("learner_id"),
                    formatTimestamp(rs.getTimestamp("created_at")),
                )
            },
            orgId,
            learnerId,
        )
    }

    /**
     * ブックマークトグル（存在時 DELETE → null、不存在時 INSERT）。
     *
     * @return 追加時 [Bookmark]、削除時 null
     */
    @Transactional
    fun toggleBookmark(orgId: String, videoId: String, learnerId: String): Bookmark? {
        return try {
            val existing = jdbc.queryForObject(
                """
                SELECT id, video_id, learner_id, created_at FROM bookmarks
                WHERE org_id = ? AND video_id = ? AND learner_id = ?
                """.trimIndent(),
                { rs, _ ->
                    Bookmark(
                        rs.requireStr("id"),
                        rs.requireStr("video_id"),
                        rs.requireStr("learner_id"),
                        formatTimestamp(rs.getTimestamp("created_at")),
                    )
                },
                orgId,
                videoId,
                learnerId,
            )!!
            jdbc.update("DELETE FROM bookmarks WHERE id = ?", existing.id)
            null
        } catch (_: EmptyResultDataAccessException) {
            val id = Ids.random("bm_")
            val now = Timestamp.from(Dates.now())
            jdbc.update(
                """
                INSERT INTO bookmarks (id, org_id, video_id, learner_id, created_at)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                id,
                orgId,
                videoId,
                learnerId,
                now,
            )
            Bookmark(id, videoId, learnerId, Dates.formatRequired(now.toInstant()))
        }
    }

    /** クイズ一覧（videoId 省略可、設問・選択肢をネスト取得）。 */
    fun listQuizzes(orgId: String, videoId: String?): List<Quiz> {
        val sql = StringBuilder(
            """
            SELECT id, COALESCE(video_id, '') AS video_id, title, passing_score FROM quizzes WHERE org_id = ?
            """.trimIndent(),
        )
        val args = ArrayList<Any>()
        args.add(orgId)
        if (!videoId.isNullOrBlank()) {
            sql.append(" AND video_id = ?")
            args.add(videoId)
        }
        val quizzes = jdbc.query(
            sql.toString(),
            { rs, _ ->
                Quiz(
                    rs.requireStr("id"),
                    rs.requireStr("video_id"),
                    rs.requireStr("title"),
                    rs.getInt("passing_score"),
                    emptyList(),
                )
            },
            *args.toTypedArray(),
        )
        return quizzes.map { quiz ->
            Quiz(quiz.id, quiz.videoId, quiz.title, quiz.passingScore, loadQuestions(quiz.id))
        }
    }

    /** クイズ 1 件取得（設問・選択肢付き）。不存在時 null。 */
    fun getQuiz(orgId: String, id: String): Quiz? {
        return try {
            val quiz = jdbc.queryForObject(
                """
                SELECT id, COALESCE(video_id, '') AS video_id, title, passing_score FROM quizzes
                WHERE org_id = ? AND id = ?
                """.trimIndent(),
                { rs, _ ->
                    Quiz(
                        rs.requireStr("id"),
                        rs.requireStr("video_id"),
                        rs.requireStr("title"),
                        rs.getInt("passing_score"),
                        emptyList(),
                    )
                },
                orgId,
                id,
            )!!
            Quiz(quiz.id, quiz.videoId, quiz.title, quiz.passingScore, loadQuestions(quiz.id))
        } catch (_: EmptyResultDataAccessException) {
            null
        }
    }

    /** クイズ受験履歴一覧（完了日降順）。 */
    fun listAttempts(orgId: String, learnerId: String): List<QuizAttempt> {
        return jdbc.query(
            """
            SELECT id, quiz_id, learner_id, score, passed, completed_at FROM quiz_attempts
            WHERE org_id = ? AND learner_id = ? ORDER BY completed_at DESC
            """.trimIndent(),
            { rs, _ ->
                QuizAttempt(
                    rs.requireStr("id"),
                    rs.requireStr("quiz_id"),
                    rs.requireStr("learner_id"),
                    rs.getInt("score"),
                    rs.getBoolean("passed"),
                    formatTimestamp(rs.getTimestamp("completed_at")),
                )
            },
            orgId,
            learnerId,
        )
    }

    /**
     * クイズ回答を採点し quiz_attempts に INSERT。
     *
     * @param answers 各設問の選択肢インデックス（不足分は不正解扱い）
     * @throws IllegalArgumentException クイズ不存在
     */
    @Transactional
    fun submitAttempt(orgId: String, quizId: String, learnerId: String, answers: List<Int>?): QuizAttempt {
        val quiz = getQuiz(orgId, quizId)
            ?: throw IllegalArgumentException("quiz not found: $quizId")
        var correct = 0
        val questions = quiz.questions
        for (i in questions.indices) {
            if (answers != null && i < answers.size && answers[i] == questions[i].correctIndex) {
                correct++
            }
        }
        val score = if (questions.isEmpty()) 0 else (correct.toDouble() / questions.size * 100).toInt()
        val passed = score >= quiz.passingScore
        val id = Ids.random("qa_")
        val now = Timestamp.from(Dates.now())
        jdbc.update(
            """
            INSERT INTO quiz_attempts (id, org_id, quiz_id, learner_id, score, passed, completed_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            id,
            orgId,
            quizId,
            learnerId,
            score,
            passed,
            now,
        )
        return QuizAttempt(id, quizId, learnerId, score, passed, Dates.formatRequired(now.toInstant()))
    }

    /** 修了証一覧（発行日降順）。 */
    fun listCertificates(orgId: String, learnerId: String): List<Certificate> {
        return jdbc.query(
            """
            SELECT id, path_id, learner_id, title, issued_at FROM certificates
            WHERE org_id = ? AND learner_id = ? ORDER BY issued_at DESC
            """.trimIndent(),
            { rs, _ ->
                Certificate(
                    rs.requireStr("id"),
                    rs.requireStr("path_id"),
                    rs.requireStr("learner_id"),
                    rs.requireStr("title"),
                    formatTimestamp(rs.getTimestamp("issued_at")),
                )
            },
            orgId,
            learnerId,
        )
    }

    /**
     * 学習パスに登録（enrollments UPSERT + enrolled_count 加算）。
     *
     * 重複登録時は ON CONFLICT DO NOTHING（カウントは加算される点に注意）。
     */
    @Transactional
    fun enrollPath(orgId: String, pathId: String, learnerId: String) {
        jdbc.update(
            """
            INSERT INTO enrollments (org_id, path_id, learner_id) VALUES (?, ?, ?)
            ON CONFLICT DO NOTHING
            """.trimIndent(),
            orgId,
            pathId,
            learnerId,
        )
        jdbc.update(
            """
            UPDATE learning_paths SET enrolled_count = enrolled_count + 1
            WHERE id = ? AND org_id = ?
            """.trimIndent(),
            pathId,
            orgId,
        )
    }

    /** クイズ ID から設問・選択肢を N+1 取得して組み立て。 */
    private fun loadQuestions(quizId: String): List<QuizQuestion> {
        val questions = jdbc.query(
            """
            SELECT id, prompt, correct_index FROM quiz_questions
            WHERE quiz_id = ? ORDER BY sort_order
            """.trimIndent(),
            { rs, _ ->
                QuizQuestion(
                    rs.requireStr("id"),
                    rs.requireStr("prompt"),
                    emptyList(),
                    rs.getInt("correct_index"),
                )
            },
            quizId,
        ).toMutableList()
        for (i in questions.indices) {
            val q = questions[i]
            val choices = jdbc.query(
                """
                SELECT id, label FROM quiz_choices WHERE question_id = ? ORDER BY sort_order
                """.trimIndent(),
                { rs, _ -> QuizChoice(rs.requireStr("id"), rs.requireStr("label")) },
                q.id,
            )
            questions[i] = QuizQuestion(q.id, q.prompt, choices, q.correctIndex)
        }
        return questions
    }

    /** Timestamp を ISO 形式文字列に変換。 */
    private fun formatTimestamp(ts: Timestamp?): String {
        return if (ts == null) Dates.formatRequired(Dates.now()) else Dates.formatRequired(ts.toInstant())
    }
}
