package jp.andpad.api.graphql

import jp.andpad.api.domain.AttendanceRecord
import jp.andpad.api.domain.BudgetLineItem
import jp.andpad.api.domain.ConstructionProject
import jp.andpad.api.domain.Contract
import jp.andpad.api.domain.ContractTemplate
import jp.andpad.api.domain.CostEntry
import jp.andpad.api.domain.CrmContact
import jp.andpad.api.domain.CrmInteraction
import jp.andpad.api.domain.DxInitiative
import jp.andpad.api.domain.ExtendedTypes.ApiIntegration
import jp.andpad.api.domain.ExtendedTypes.BimModel
import jp.andpad.api.domain.ExtendedTypes.ConsultMessageReply
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.LeaveRequest
import jp.andpad.api.domain.LearningTypes.AnalyticsInsight
import jp.andpad.api.domain.LearningTypes.Bookmark
import jp.andpad.api.domain.LearningTypes.LearningPath
import jp.andpad.api.domain.LearningTypes.QuizAttempt
import jp.andpad.api.domain.LearningTypes.Video
import jp.andpad.api.domain.LearningTypes.VideoNote
import jp.andpad.api.domain.LearningTypes.WatchProgress
import jp.andpad.api.domain.Organization
import jp.andpad.api.domain.ProjectBudget
import jp.andpad.api.domain.ProjectModuleRecord
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.graphql.input.CreateBudgetLineItemInput
import jp.andpad.api.graphql.input.CreateConstructionProjectInput
import jp.andpad.api.graphql.input.CreateContractInput
import jp.andpad.api.graphql.input.CreateCostEntryInput
import jp.andpad.api.graphql.input.CreateCrmContactInput
import jp.andpad.api.graphql.input.CreateDxInitiativeInput
import jp.andpad.api.graphql.input.CreateLeaveRequestInput
import jp.andpad.api.graphql.input.CreateProjectBudgetInput
import jp.andpad.api.graphql.input.CreateProjectModuleRecordInput
import jp.andpad.api.graphql.input.LearningInputs.CreateApiIntegrationInput
import jp.andpad.api.graphql.input.LearningInputs.CreateBimModelInput
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput
import jp.andpad.api.graphql.input.LearningInputs.CreateVideoNoteInput
import jp.andpad.api.graphql.input.LearningInputs.SubmitQuizAttemptInput
import jp.andpad.api.graphql.input.LearningInputs.UpdateWatchProgressInput
import jp.andpad.api.graphql.input.UpdateOrganizationInput
import jp.andpad.api.service.BudgetService
import jp.andpad.api.service.ConstructionService
import jp.andpad.api.service.ConsultService
import jp.andpad.api.service.ExtendedService
import jp.andpad.api.service.LearningStubService
import jp.andpad.api.service.OrganizationService
import jp.andpad.api.service.SaasService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class MutationController(
    private val organizationService: OrganizationService,
    private val learningStubService: LearningStubService,
    private val saasService: SaasService,
    private val constructionService: ConstructionService,
    private val budgetService: BudgetService,
    private val extendedService: ExtendedService,
    private val consultService: ConsultService,
) {

    @MutationMapping
    fun updateOrganization(@Argument input: UpdateOrganizationInput): Organization =
        organizationService.updateOrganization(input)

    @MutationMapping
    fun generateAnalyticsInsight(@Argument periodDays: Int): AnalyticsInsight =
        extendedService.generateAnalyticsInsight(periodDays)

    @MutationMapping
    fun updateWatchProgress(@Argument input: UpdateWatchProgressInput): WatchProgress =
        learningStubService.updateWatchProgress(input)

    @MutationMapping
    fun recordVideoView(@Argument videoId: String): Video =
        learningStubService.recordVideoView(videoId)

    @MutationMapping
    fun createVideoNote(@Argument input: CreateVideoNoteInput): VideoNote =
        learningStubService.createVideoNote(input)

    @MutationMapping
    fun deleteVideoNote(@Argument id: String): Boolean =
        learningStubService.deleteVideoNote(id)

    @MutationMapping
    fun toggleBookmark(
        @Argument videoId: String,
        @Argument learnerId: String,
    ): Bookmark = learningStubService.toggleBookmark(videoId, learnerId)

    @MutationMapping
    fun enrollLearningPath(
        @Argument pathId: String,
        @Argument learnerId: String,
    ): LearningPath = learningStubService.enrollLearningPath(pathId, learnerId)

    @MutationMapping
    fun submitQuizAttempt(@Argument input: SubmitQuizAttemptInput): QuizAttempt =
        learningStubService.submitQuizAttempt(input)

    @MutationMapping
    fun createDxInitiative(@Argument input: CreateDxInitiativeInput): DxInitiative =
        saasService.createDxInitiative(input)

    @MutationMapping
    fun createCrmContact(@Argument input: CreateCrmContactInput): CrmContact =
        saasService.createCrmContact(input)

    @MutationMapping
    fun createCrmInteraction(
        @Argument contactId: String,
        @Argument kind: String,
        @Argument summary: String,
    ): CrmInteraction = saasService.createCrmInteraction(contactId, kind, summary)

    @MutationMapping
    fun clockIn(@Argument note: String?): AttendanceRecord = saasService.clockIn(note)

    @MutationMapping
    fun clockOut(): AttendanceRecord = saasService.clockOut()

    @MutationMapping
    fun createLeaveRequest(@Argument input: CreateLeaveRequestInput): LeaveRequest =
        saasService.createLeaveRequest(input)

    @MutationMapping
    fun approveLeaveRequest(@Argument id: String): LeaveRequest =
        saasService.approveLeaveRequest(id)

    @MutationMapping
    fun createContractTemplate(
        @Argument name: String,
        @Argument body: String,
    ): ContractTemplate = saasService.createContractTemplate(name, body)

    @MutationMapping
    fun createContract(@Argument input: CreateContractInput): Contract =
        saasService.createContract(input)

    @MutationMapping
    fun signContract(@Argument id: String): Contract = saasService.signContract(id)

    @MutationMapping
    fun sendConsultMessage(
        @Argument threadId: String?,
        @Argument message: String,
    ): ConsultMessageReply = consultService.sendMessage(threadId, message)

    @MutationMapping
    fun createRagDocument(@Argument input: CreateRagDocumentInput): RagDocument =
        consultService.createRagDocument(input)

    @MutationMapping
    fun setSaasModuleEnabled(
        @Argument code: SaasModuleCode,
        @Argument enabled: Boolean,
    ): SaasModule = saasService.setModuleEnabled(code, enabled)

    @MutationMapping
    fun createConstructionProject(@Argument input: CreateConstructionProjectInput): ConstructionProject =
        constructionService.createProject(input)

    @MutationMapping
    fun createProjectModuleRecord(@Argument input: CreateProjectModuleRecordInput): ProjectModuleRecord =
        constructionService.createModuleRecord(input)

    @MutationMapping
    fun createApiIntegration(@Argument input: CreateApiIntegrationInput): ApiIntegration =
        extendedService.createApiIntegration(input)

    @MutationMapping
    fun syncApiIntegration(@Argument id: String): ApiIntegration =
        extendedService.syncApiIntegration(id)

    @MutationMapping
    fun createBimModel(@Argument input: CreateBimModelInput): BimModel =
        extendedService.createBimModel(input)

    @MutationMapping
    fun createProjectBudget(@Argument input: CreateProjectBudgetInput): ProjectBudget =
        budgetService.createBudget(input)

    @MutationMapping
    fun createBudgetLineItem(@Argument input: CreateBudgetLineItemInput): BudgetLineItem =
        budgetService.createLineItem(input)

    @MutationMapping
    fun createCostEntry(@Argument input: CreateCostEntryInput): CostEntry =
        budgetService.createCostEntry(input)

    @MutationMapping
    fun approveProjectBudget(@Argument id: String): ProjectBudget =
        budgetService.approveBudget(id)

    @MutationMapping
    fun createCostFromBilling(
        @Argument billingRecordId: String,
        @Argument projectId: String,
    ): CostEntry = budgetService.createCostFromBilling(billingRecordId, projectId)
}
