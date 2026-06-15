package jp.andpad.api.graphql

import jp.andpad.api.domain.AttendanceRecord
import jp.andpad.api.domain.BudgetDashboard
import jp.andpad.api.domain.BudgetLineItem
import jp.andpad.api.domain.BudgetType
import jp.andpad.api.domain.ConstructionProject
import jp.andpad.api.domain.Contract
import jp.andpad.api.domain.ContractTemplate
import jp.andpad.api.domain.CostEntry
import jp.andpad.api.domain.CrmContact
import jp.andpad.api.domain.CrmInteraction
import jp.andpad.api.domain.DxInitiative
import jp.andpad.api.domain.ExtendedTypes.AndpadAnalyticsDashboard
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration
import jp.andpad.api.domain.ExtendedTypes.BimModel
import jp.andpad.api.domain.ExtendedTypes.ConsultThread
import jp.andpad.api.domain.ExtendedTypes.RagAnswer
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit
import jp.andpad.api.domain.Health
import jp.andpad.api.domain.LeaveRequest
import jp.andpad.api.domain.LearningTypes.AnalyticsBoard
import jp.andpad.api.domain.LearningTypes.Bookmark
import jp.andpad.api.domain.LearningTypes.Certificate
import jp.andpad.api.domain.LearningTypes.DashboardStats
import jp.andpad.api.domain.LearningTypes.Instructor
import jp.andpad.api.domain.LearningTypes.LearningPath
import jp.andpad.api.domain.LearningTypes.Quiz
import jp.andpad.api.domain.LearningTypes.QuizAttempt
import jp.andpad.api.domain.LearningTypes.Video
import jp.andpad.api.domain.LearningTypes.VideoNote
import jp.andpad.api.domain.LearningTypes.VideoPage
import jp.andpad.api.domain.LearningTypes.WatchProgress
import jp.andpad.api.domain.Organization
import jp.andpad.api.domain.ProjectBudget
import jp.andpad.api.domain.ProjectBudgetSummary
import jp.andpad.api.domain.ProjectModuleRecord
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.domain.Session
import jp.andpad.api.domain.SkillLevel
import jp.andpad.api.domain.TeamMember
import jp.andpad.api.domain.UsageSummary
import jp.andpad.api.domain.VideoCategory
import jp.andpad.api.service.AuthService
import jp.andpad.api.service.BudgetService
import jp.andpad.api.service.ConstructionService
import jp.andpad.api.service.ConsultService
import jp.andpad.api.service.ExtendedService
import jp.andpad.api.service.LearningStubService
import jp.andpad.api.service.OrganizationService
import jp.andpad.api.service.SaasService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class QueryController(
    private val authService: AuthService,
    private val organizationService: OrganizationService,
    private val learningStubService: LearningStubService,
    private val saasService: SaasService,
    private val constructionService: ConstructionService,
    private val budgetService: BudgetService,
    private val extendedService: ExtendedService,
    private val consultService: ConsultService,
) {

    @QueryMapping
    fun health(): Health = Health(true, "andpad-api", "2.0.0-saas")

    @QueryMapping
    fun dashboard(): DashboardStats = learningStubService.dashboard()

    @QueryMapping
    fun currentSession(): Session? = authService.currentSession()

    @QueryMapping
    fun organization(): Organization = organizationService.getOrganization()

    @QueryMapping
    fun usageSummary(): UsageSummary = organizationService.usageSummary()

    @QueryMapping
    fun teamMembers(): List<TeamMember> = organizationService.teamMembers()

    @QueryMapping
    fun analyticsBoard(@Argument periodDays: Int): AnalyticsBoard =
        learningStubService.analyticsBoard(periodDays)

    @QueryMapping
    fun videos(
        @Argument category: VideoCategory?,
        @Argument skillLevel: SkillLevel?,
        @Argument search: String?,
        @Argument page: Int?,
        @Argument pageSize: Int?,
    ): VideoPage = learningStubService.videos(category, skillLevel, search, page, pageSize)

    @QueryMapping
    fun video(@Argument id: String): Video = learningStubService.video(id)

    @QueryMapping
    fun featuredVideos(): List<Video> = learningStubService.featuredVideos()

    @QueryMapping
    fun instructors(): List<Instructor> = learningStubService.instructors()

    @QueryMapping
    fun instructor(@Argument id: String): Instructor = learningStubService.instructor(id)

    @QueryMapping
    fun learningPaths(
        @Argument category: VideoCategory?,
        @Argument skillLevel: SkillLevel?,
    ): List<LearningPath> = learningStubService.learningPaths(category, skillLevel)

    @QueryMapping
    fun learningPath(@Argument id: String): LearningPath = learningStubService.learningPath(id)

    @QueryMapping
    fun myProgress(@Argument learnerId: String): List<WatchProgress> =
        learningStubService.myProgress(learnerId)

    @QueryMapping
    fun myBookmarks(@Argument learnerId: String): List<Bookmark> =
        learningStubService.myBookmarks(learnerId)

    @QueryMapping
    fun videoNotes(
        @Argument videoId: String,
        @Argument learnerId: String,
    ): List<VideoNote> = learningStubService.videoNotes(videoId, learnerId)

    @QueryMapping
    fun quizzes(@Argument videoId: String): List<Quiz> = learningStubService.quizzes(videoId)

    @QueryMapping
    fun quiz(@Argument id: String): Quiz = learningStubService.quiz(id)

    @QueryMapping
    fun myQuizAttempts(@Argument learnerId: String): List<QuizAttempt> =
        learningStubService.myQuizAttempts(learnerId)

    @QueryMapping
    fun myCertificates(@Argument learnerId: String): List<Certificate> =
        learningStubService.myCertificates(learnerId)

    @QueryMapping
    fun saasModules(): List<SaasModule> = saasService.saasModules()

    @QueryMapping
    fun dxInitiatives(): List<DxInitiative> = saasService.dxInitiatives()

    @QueryMapping
    fun crmContacts(): List<CrmContact> = saasService.crmContacts()

    @QueryMapping
    fun crmInteractions(@Argument contactId: String): List<CrmInteraction> =
        saasService.crmInteractions(contactId)

    @QueryMapping
    fun attendanceRecords(): List<AttendanceRecord> = saasService.attendanceRecords()

    @QueryMapping
    fun leaveRequests(): List<LeaveRequest> = saasService.leaveRequests()

    @QueryMapping
    fun contractTemplates(): List<ContractTemplate> = saasService.contractTemplates()

    @QueryMapping
    fun contracts(): List<Contract> = saasService.contracts()

    @QueryMapping
    fun consultThreads(): List<ConsultThread> = consultService.listThreads()

    @QueryMapping
    fun consultThread(@Argument id: String): ConsultThread = consultService.getThread(id)

    @QueryMapping
    fun ragDocuments(): List<RagDocument> = consultService.listRagDocuments()

    @QueryMapping
    fun ragSearch(@Argument query: String, @Argument limit: Int): List<RagSearchHit> =
        consultService.searchRag(query, limit)

    @QueryMapping
    fun ragAnswer(@Argument query: String): RagAnswer = consultService.ragAnswer(query)

    @QueryMapping
    fun constructionProjects(): List<ConstructionProject> = constructionService.listProjects()

    @QueryMapping
    fun projectModuleRecords(
        @Argument moduleCode: SaasModuleCode?,
        @Argument projectId: String?,
    ): List<ProjectModuleRecord> = constructionService.listModuleRecords(moduleCode, projectId)

    @QueryMapping
    fun andpadAnalytics(@Argument periodDays: Int): AndpadAnalyticsDashboard =
        extendedService.andpadAnalytics(periodDays)

    @QueryMapping
    fun apiIntegrations(): List<ApiIntegration> = extendedService.listApiIntegrations()

    @QueryMapping
    fun bimModels(@Argument projectId: String): List<BimModel> =
        extendedService.listBimModels(projectId)

    @QueryMapping
    fun bimModel(@Argument id: String): BimModel = extendedService.getBimModel(id)

    @QueryMapping
    fun projectBudgets(
        @Argument projectId: String?,
        @Argument budgetType: BudgetType?,
    ): List<ProjectBudget> = budgetService.listBudgets(projectId, budgetType)

    @QueryMapping
    fun budgetLineItems(@Argument budgetId: String): List<BudgetLineItem> =
        budgetService.listLineItems(budgetId)

    @QueryMapping
    fun costEntries(
        @Argument projectId: String?,
        @Argument lineItemId: String?,
    ): List<CostEntry> = budgetService.listCostEntries(projectId, lineItemId)

    @QueryMapping
    fun budgetDashboard(@Argument projectId: String): BudgetDashboard =
        budgetService.budgetDashboard(projectId)

    @QueryMapping
    fun projectBudgetSummaries(): List<ProjectBudgetSummary> = budgetService.listBudgetSummaries()
}
