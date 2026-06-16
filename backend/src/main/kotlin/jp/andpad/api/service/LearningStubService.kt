package jp.andpad.api.service

import jp.andpad.api.domain.LearningActivityKind
import jp.andpad.api.domain.LearningTypes.AnalyticsBoard
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight
import jp.andpad.api.domain.LearningTypes.AnalyticsKpi
import jp.andpad.api.domain.LearningTypes.Bookmark
import jp.andpad.api.domain.LearningTypes.Certificate
import jp.andpad.api.domain.LearningTypes.DashboardStats
import jp.andpad.api.domain.LearningTypes.Instructor
import jp.andpad.api.domain.LearningTypes.LearningActivityEvent
import jp.andpad.api.domain.LearningTypes.LearningPath
import jp.andpad.api.domain.LearningTypes.Quiz
import jp.andpad.api.domain.LearningTypes.QuizAttempt
import jp.andpad.api.domain.LearningTypes.Video
import jp.andpad.api.domain.LearningTypes.VideoNote
import jp.andpad.api.domain.LearningTypes.VideoPage
import jp.andpad.api.domain.LearningTypes.WatchProgress
import jp.andpad.api.domain.SkillLevel
import jp.andpad.api.domain.VideoCategory
import jp.andpad.api.graphql.input.LearningInputs.CreateVideoNoteInput
import jp.andpad.api.graphql.input.LearningInputs.SubmitQuizAttemptInput
import jp.andpad.api.graphql.input.LearningInputs.UpdateWatchProgressInput
import jp.andpad.api.repository.EngagementRepository
import jp.andpad.api.repository.LearningRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

/**
 * ラーニング機能サービス（動画・講師・パス・エンゲージメント）。
 *
 * **責務**: [LearningRepository] と [EngagementRepository] を組み合わせ、
 * カタログ閲覧・進捗更新・クイズ・ブックマーク等を提供。
 * 分析ボード／インサイトはスタブ実装。
 *
 * **テナント分離**: 全操作で TenantContext.orgId() を使用。
 */
@Service
class LearningStubService(
    private val learningRepository: LearningRepository,
    private val engagementRepository: EngagementRepository,
) {

    /** ラーニングダッシュボード統計。 */
    fun dashboard(): DashboardStats {
        return learningRepository.dashboard(TenantContext.orgId())
    }

    /** 動画一覧（ページネーション、page 省略時 1、pageSize 省略時 20）。 */
    fun videos(
        category: VideoCategory?,
        skillLevel: SkillLevel?,
        search: String?,
        page: Int?,
        pageSize: Int?,
    ): VideoPage {
        val p = if (page == null || page < 1) 1 else page
        val size = if (pageSize == null || pageSize < 1) 20 else pageSize
        return learningRepository.paginateVideos(TenantContext.orgId(), category, skillLevel, search, p, size)
    }

    /**
     * 動画 1 件取得。
     *
     * @throws IllegalArgumentException 不存在時
     */
    fun video(id: String): Video {
        return learningRepository.getVideo(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("video not found")
    }

    /** おすすめ動画一覧。 */
    fun featuredVideos(): List<Video> {
        return learningRepository.featuredVideos(TenantContext.orgId())
    }

    /** 講師一覧。 */
    fun instructors(): List<Instructor> {
        return learningRepository.listInstructors(TenantContext.orgId())
    }

    /**
     * 講師 1 件取得。
     *
     * @throws IllegalArgumentException 不存在時
     */
    fun instructor(id: String): Instructor {
        return learningRepository.getInstructor(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("instructor not found")
    }

    /** 学習パス一覧。 */
    fun learningPaths(category: VideoCategory?, skillLevel: SkillLevel?): List<LearningPath> {
        return learningRepository.listPaths(TenantContext.orgId(), category, skillLevel)
    }

    /**
     * 学習パス 1 件取得。
     *
     * @throws IllegalArgumentException 不存在時
     */
    fun learningPath(id: String): LearningPath {
        return learningRepository.getPath(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("path not found")
    }

    /** 学習者の視聴進捗一覧。 */
    fun myProgress(learnerId: String): List<WatchProgress> {
        return engagementRepository.listProgress(TenantContext.orgId(), learnerId)
    }

    /** 学習者のブックマーク一覧。 */
    fun myBookmarks(learnerId: String): List<Bookmark> {
        return engagementRepository.listBookmarks(TenantContext.orgId(), learnerId)
    }

    /** 動画ノート一覧。 */
    fun videoNotes(videoId: String, learnerId: String): List<VideoNote> {
        return engagementRepository.listNotes(TenantContext.orgId(), videoId, learnerId)
    }

    /** 動画に紐づくクイズ一覧。 */
    fun quizzes(videoId: String): List<Quiz> {
        return engagementRepository.listQuizzes(TenantContext.orgId(), videoId)
    }

    /**
     * クイズ 1 件取得。
     *
     * @throws IllegalArgumentException 不存在時
     */
    fun quiz(id: String): Quiz {
        return engagementRepository.getQuiz(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("quiz not found")
    }

    /** 学習者のクイズ受験履歴。 */
    fun myQuizAttempts(learnerId: String): List<QuizAttempt> {
        return engagementRepository.listAttempts(TenantContext.orgId(), learnerId)
    }

    /** 学習者の修了証一覧。 */
    fun myCertificates(learnerId: String): List<Certificate> {
        return engagementRepository.listCertificates(TenantContext.orgId(), learnerId)
    }

    /** ラーニング分析ボード（KPI は dashboard から算出、週次トレンドはスタブ比率）。 */
    fun analyticsBoard(periodDays: Int): AnalyticsBoard {
        val d = dashboard()
        return AnalyticsBoard(
            periodDays,
            listOf(
                AnalyticsKpi("watch_hours", d.watchHoursThisMonth, "h", null),
                AnalyticsKpi("completions", d.completionsThisMonth.toDouble(), "", null),
                AnalyticsKpi("active_learners", d.activeLearners.toDouble(), "", null),
                AnalyticsKpi("video_library", d.videosTotal.toDouble(), "", null),
            ),
            listOf(
                d.watchHoursThisMonth * 0.2,
                d.watchHoursThisMonth * 0.25,
                d.watchHoursThisMonth * 0.3,
                d.watchHoursThisMonth * 0.25,
            ),
            listOf(),
            listOf(),
            if (d.activeLearners > 0) 72.0 else 0.0,
        )
    }

    /** ラーニング分析インサイト（スタブ固定文言）。 */
    fun analyticsInsight(periodDays: Int): AnalyticsInsight {
        return AnalyticsInsight(
            "Learning analytics stub",
            listOf(),
            listOf(),
            listOf("Connect learning data source"),
            Dates.formatRequired(Dates.now()),
        )
    }

    /** 視聴進捗 UPSERT。 */
    fun updateWatchProgress(input: UpdateWatchProgressInput): WatchProgress {
        val progress = WatchProgress(
            "",
            input.videoId,
            input.learnerId,
            input.positionSec,
            java.lang.Boolean.TRUE == input.completed,
            Dates.formatRequired(Dates.now()),
        )
        return learningRepository.updateProgress(TenantContext.orgId(), progress)
    }

    /** 視聴回数 +1（更新失敗時は getVideo フォールバック）。 */
    fun recordVideoView(videoId: String): Video {
        val updated = learningRepository.incrementViewCount(TenantContext.orgId(), videoId)
        return updated ?: video(videoId)
    }

    /** 動画ノート作成。 */
    fun createVideoNote(input: CreateVideoNoteInput): VideoNote {
        val note = VideoNote(
            "",
            input.videoId,
            input.learnerId,
            input.timestampSec,
            input.body,
            Dates.formatRequired(Dates.now()),
        )
        return engagementRepository.createNote(TenantContext.orgId(), note)
    }

    /** 動画ノート削除。 */
    fun deleteVideoNote(id: String): Boolean {
        return engagementRepository.deleteNote(TenantContext.orgId(), id)
    }

    /**
     * ブックマークトグル。
     *
     * @throws IllegalStateException 削除時（null 返却）— 呼び出し側で例外化
     */
    fun toggleBookmark(videoId: String, learnerId: String): Bookmark {
        return engagementRepository.toggleBookmark(TenantContext.orgId(), videoId, learnerId)
            ?: throw IllegalStateException("bookmark toggle failed")
    }

    /** 学習パス登録後、最新パス情報を返す。 */
    fun enrollLearningPath(pathId: String, learnerId: String): LearningPath {
        engagementRepository.enrollPath(TenantContext.orgId(), pathId, learnerId)
        return learningPath(pathId)
    }

    /** クイズ回答提出・採点。 */
    fun submitQuizAttempt(input: SubmitQuizAttemptInput): QuizAttempt {
        return engagementRepository.submitAttempt(
            TenantContext.orgId(),
            input.quizId,
            input.learnerId,
            input.answers,
        )
    }

    /** 学習アクティビティイベント（スタブ固定値）。 */
    fun learningActivityEvent(learnerId: String): LearningActivityEvent {
        return LearningActivityEvent(
            LearningActivityKind.PROGRESS_UPDATED,
            learnerId,
            "",
            "",
            "",
            "stub",
            Dates.formatRequired(Dates.now()),
        )
    }
}
