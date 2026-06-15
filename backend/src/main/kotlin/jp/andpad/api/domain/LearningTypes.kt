package jp.andpad.api.domain

object LearningTypes {
    data class DashboardStats(
        val videosTotal: Int,
        val learningPathsTotal: Int,
        val quizzesTotal: Int,
        val completionsThisMonth: Int,
        val watchHoursThisMonth: Double,
        val activeLearners: Int,
    )

    data class Instructor(
        val id: String,
        val name: String,
        val title: String,
        val specialty: String,
        val bio: String,
        val avatarUrl: String,
        val videoCount: Int,
    )

    data class Video(
        val id: String,
        val title: String,
        val description: String,
        val category: VideoCategory,
        val procedure: String,
        val skillLevel: SkillLevel,
        val durationSec: Int,
        val thumbnailUrl: String,
        val videoUrl: String,
        val instructorId: String,
        val instructorName: String,
        val tags: List<String>,
        val viewCount: Int,
        val publishedAt: String,
        val featured: Boolean,
    )

    data class LearningPath(
        val id: String,
        val title: String,
        val description: String,
        val category: VideoCategory,
        val skillLevel: SkillLevel,
        val videoIds: List<String>,
        val estimatedMinutes: Int,
        val enrolledCount: Int,
        val certificateTitle: String,
    )

    data class WatchProgress(
        val id: String,
        val videoId: String,
        val learnerId: String,
        val positionSec: Int,
        val completed: Boolean,
        val updatedAt: String,
    )

    data class VideoNote(
        val id: String,
        val videoId: String,
        val learnerId: String,
        val timestampSec: Int,
        val body: String,
        val createdAt: String,
    )

    data class QuizChoice(val id: String, val label: String)

    data class QuizQuestion(val id: String, val prompt: String, val choices: List<QuizChoice>, val correctIndex: Int)

    data class Quiz(
        val id: String,
        val videoId: String,
        val title: String,
        val passingScore: Int,
        val questions: List<QuizQuestion>,
    )

    data class QuizAttempt(
        val id: String,
        val quizId: String,
        val learnerId: String,
        val score: Int,
        val passed: Boolean,
        val completedAt: String,
    )

    data class Bookmark(val id: String, val videoId: String, val learnerId: String, val createdAt: String)

    data class Certificate(val id: String, val pathId: String, val learnerId: String, val title: String, val issuedAt: String)

    data class PageInfo(val total: Int, val page: Int, val pageSize: Int, val totalPages: Int)

    data class VideoPage(val items: List<Video>, val pageInfo: PageInfo)

    data class LearningActivityEvent(
        val kind: LearningActivityKind,
        val learnerId: String,
        val videoId: String,
        val pathId: String,
        val quizId: String,
        val message: String,
        val occurredAt: String,
    )

    data class AnalyticsKpi(val label: String, val value: Double, val unit: String, val trendPct: Double?)

    data class CategoryMetric(val category: VideoCategory, val count: Int)

    data class VideoMetric(val videoId: String, val title: String, val views: Int, val completions: Int)

    data class AnalyticsBoard(
        val periodDays: Int,
        val kpis: List<AnalyticsKpi>,
        val watchHoursByWeek: List<Double>,
        val completionsByCategory: List<CategoryMetric>,
        val topVideos: List<VideoMetric>,
        val learnerEngagementScore: Double,
    )

    data class AnalyticsInsight(
        val summary: String,
        val strengths: List<String>,
        val risks: List<String>,
        val recommendations: List<String>,
        val generatedAt: String,
    )
}
