package jp.andpad.api.domain

object ExtendedTypes {
    data class AndpadAnalyticsKpi(val label: String, val value: Double, val unit: String, val trendPct: Double?)

    data class ProjectStatusCount(val status: ConstructionProjectStatus, val count: Int)

    data class ModuleUsageMetric(val moduleCode: SaasModuleCode, val moduleName: String, val recordCount: Int)

    data class AndpadAnalyticsDashboard(
        val periodDays: Int,
        val kpis: List<AndpadAnalyticsKpi>,
        val projectsByStatus: List<ProjectStatusCount>,
        val moduleUsage: List<ModuleUsageMetric>,
        val billingTotal: Double,
        val activeProjects: Int,
        val recordsByWeek: List<Double>,
        val projectHealthScore: Double,
        val budgetTotal: Double,
        val costTotal: Double,
        val budgetVariancePct: Double,
        val costByMonth: List<MonthlyCostMetric>,
        val generatedAt: String,
    )

    data class ApiIntegration(
        val id: String,
        val name: String,
        val provider: String,
        val endpointUrl: String,
        val apiKeyHint: String,
        val status: String,
        val lastSyncAt: String,
        val createdAt: String,
    )

    data class BimModel(
        val id: String,
        val projectId: String,
        val projectName: String,
        val title: String,
        val format: String,
        val viewerUrl: String,
        val fileSizeMb: Double?,
        val status: String,
        val uploadedBy: String,
        val createdAt: String,
    )

    data class ConsultMessage(val id: String, val role: String, val content: String, val createdAt: String)

    data class ConsultThread(val id: String, val title: String, val createdAt: String, val messages: List<ConsultMessage>)

    data class ConsultMessageReply(
        val threadId: String,
        val userMessage: ConsultMessage,
        val assistantMessage: ConsultMessage,
    )

    data class RagDocument(val id: String, val title: String, val content: String, val tags: List<String>, val createdAt: String)

    data class RagSearchHit(val documentId: String, val title: String, val snippet: String, val score: Double)

    data class RagAnswer(val answer: String, val sources: List<RagSearchHit>)
}
