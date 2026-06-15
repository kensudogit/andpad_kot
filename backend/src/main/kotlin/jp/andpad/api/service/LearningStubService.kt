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

@Service
class LearningStubService(
    private val learningRepository: LearningRepository,
    private val engagementRepository: EngagementRepository,
) {

    fun dashboard(): DashboardStats {
        return learningRepository.dashboard(TenantContext.orgId())
    }

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

    fun video(id: String): Video {
        return learningRepository.getVideo(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("video not found")
    }

    fun featuredVideos(): List<Video> {
        return learningRepository.featuredVideos(TenantContext.orgId())
    }

    fun instructors(): List<Instructor> {
        return learningRepository.listInstructors(TenantContext.orgId())
    }

    fun instructor(id: String): Instructor {
        return learningRepository.getInstructor(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("instructor not found")
    }

    fun learningPaths(category: VideoCategory?, skillLevel: SkillLevel?): List<LearningPath> {
        return learningRepository.listPaths(TenantContext.orgId(), category, skillLevel)
    }

    fun learningPath(id: String): LearningPath {
        return learningRepository.getPath(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("path not found")
    }

    fun myProgress(learnerId: String): List<WatchProgress> {
        return engagementRepository.listProgress(TenantContext.orgId(), learnerId)
    }

    fun myBookmarks(learnerId: String): List<Bookmark> {
        return engagementRepository.listBookmarks(TenantContext.orgId(), learnerId)
    }

    fun videoNotes(videoId: String, learnerId: String): List<VideoNote> {
        return engagementRepository.listNotes(TenantContext.orgId(), videoId, learnerId)
    }

    fun quizzes(videoId: String): List<Quiz> {
        return engagementRepository.listQuizzes(TenantContext.orgId(), videoId)
    }

    fun quiz(id: String): Quiz {
        return engagementRepository.getQuiz(TenantContext.orgId(), id)
            ?: throw IllegalArgumentException("quiz not found")
    }

    fun myQuizAttempts(learnerId: String): List<QuizAttempt> {
        return engagementRepository.listAttempts(TenantContext.orgId(), learnerId)
    }

    fun myCertificates(learnerId: String): List<Certificate> {
        return engagementRepository.listCertificates(TenantContext.orgId(), learnerId)
    }

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

    fun analyticsInsight(periodDays: Int): AnalyticsInsight {
        return AnalyticsInsight(
            "Learning analytics stub",
            listOf(),
            listOf(),
            listOf("Connect learning data source"),
            Dates.formatRequired(Dates.now()),
        )
    }

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

    fun recordVideoView(videoId: String): Video {
        val updated = learningRepository.incrementViewCount(TenantContext.orgId(), videoId)
        return updated ?: video(videoId)
    }

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

    fun deleteVideoNote(id: String): Boolean {
        return engagementRepository.deleteNote(TenantContext.orgId(), id)
    }

    fun toggleBookmark(videoId: String, learnerId: String): Bookmark {
        return engagementRepository.toggleBookmark(TenantContext.orgId(), videoId, learnerId)
            ?: throw IllegalStateException("bookmark toggle failed")
    }

    fun enrollLearningPath(pathId: String, learnerId: String): LearningPath {
        engagementRepository.enrollPath(TenantContext.orgId(), pathId, learnerId)
        return learningPath(pathId)
    }

    fun submitQuizAttempt(input: SubmitQuizAttemptInput): QuizAttempt {
        return engagementRepository.submitAttempt(
            TenantContext.orgId(),
            input.quizId,
            input.learnerId,
            input.answers,
        )
    }

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
