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

/**
 * GraphQL Query ルートコントローラー。
 *
 * Spring GraphQL の [QueryMapping] により、GraphQL スキーマの `Query` 型フィールドを
 * 各サービス層の読み取り操作へ委譲する。エンドポイントは `/graphql`。
 *
 * 認証は HTTP レベルでは必須ではなく、JWT（Bearer または `dv_token` Cookie）が
 * 付与されている場合のみ [jp.andpad.api.security.TenantContext] に実ユーザーが設定される。
 * 未認証時はデモテナント（`org_demo` / `user_demo`）のデータが返る。
 * 一部の相談系 Query はサービス層で [jp.andpad.api.security.TenantContext.requirePrincipal] を
 * 要求し、未認証時は [jp.andpad.api.security.UnauthorizedException] となる。
 */
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

    /**
     * API の稼働状態を返すヘルスチェック Query。
     *
     * 認証: 不要。
     */
    @QueryMapping
    fun health(): Health = Health(true, "andpad-api", "2.0.0-saas")

    /**
     * 学習モジュールのダッシュボード統計（視聴数・修了率など）を取得する。
     *
     * 認証: 任意（未認証時はデモテナントの統計）。
     */
    @QueryMapping
    fun dashboard(): DashboardStats = learningStubService.dashboard()

    /**
     * 現在ログイン中ユーザーのセッション（ユーザー・組織・ロール）を取得する。
     *
     * 認証: JWT 必須。未認証時は `null` を返し、例外は送出しない。
     */
    @QueryMapping
    fun currentSession(): Session? = authService.currentSession()

    /**
     * 現在テナントの組織プロフィール（プラン・モジュール一覧を含む）を取得する。
     *
     * 認証: 任意（未認証時はデモ組織）。
     */
    @QueryMapping
    fun organization(): Organization = organizationService.getOrganization()

    /**
     * 組織の SaaS 利用量サマリー（ストレージ・API 呼び出し等）を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun usageSummary(): UsageSummary = organizationService.usageSummary()

    /**
     * 組織に所属するチームメンバー一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun teamMembers(): List<TeamMember> = organizationService.teamMembers()

    /**
     * 指定期間の学習分析ボード（視聴トレンド・カテゴリ別集計等）を取得する。
     *
     * @param periodDays 集計対象の日数（例: 7, 30）。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun analyticsBoard(@Argument periodDays: Int): AnalyticsBoard =
        learningStubService.analyticsBoard(periodDays)

    /**
     * 学習動画を条件付きでページング取得する。
     *
     * @param category カテゴリで絞り込み。`null` の場合は全カテゴリ。
     * @param skillLevel スキルレベルで絞り込み。`null` の場合は全レベル。
     * @param search タイトル・説明の部分一致検索。`null` または空の場合は検索なし。
     * @param page 1 始まりのページ番号。`null` の場合はデフォルト（先頭ページ）。
     * @param pageSize 1 ページあたりの件数。`null` の場合はデフォルト件数。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun videos(
        @Argument category: VideoCategory?,
        @Argument skillLevel: SkillLevel?,
        @Argument search: String?,
        @Argument page: Int?,
        @Argument pageSize: Int?,
    ): VideoPage = learningStubService.videos(category, skillLevel, search, page, pageSize)

    /**
     * ID を指定して単一の学習動画を取得する。
     *
     * @param id 動画 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun video(@Argument id: String): Video = learningStubService.video(id)

    /**
     * おすすめ（フィーチャー）動画一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun featuredVideos(): List<Video> = learningStubService.featuredVideos()

    /**
     * 講師一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun instructors(): List<Instructor> = learningStubService.instructors()

    /**
     * ID を指定して単一の講師情報を取得する。
     *
     * @param id 講師 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun instructor(@Argument id: String): Instructor = learningStubService.instructor(id)

    /**
     * 学習パス一覧を条件付きで取得する。
     *
     * @param category カテゴリで絞り込み。`null` の場合は全カテゴリ。
     * @param skillLevel スキルレベルで絞り込み。`null` の場合は全レベル。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun learningPaths(
        @Argument category: VideoCategory?,
        @Argument skillLevel: SkillLevel?,
    ): List<LearningPath> = learningStubService.learningPaths(category, skillLevel)

    /**
     * ID を指定して単一の学習パスを取得する。
     *
     * @param id 学習パス ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun learningPath(@Argument id: String): LearningPath = learningStubService.learningPath(id)

    /**
     * 指定学習者の動画視聴進捗一覧を取得する。
     *
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun myProgress(@Argument learnerId: String): List<WatchProgress> =
        learningStubService.myProgress(learnerId)

    /**
     * 指定学習者のブックマーク済み動画一覧を取得する。
     *
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun myBookmarks(@Argument learnerId: String): List<Bookmark> =
        learningStubService.myBookmarks(learnerId)

    /**
     * 指定動画・学習者のメモ（タイムスタンプ付きノート）一覧を取得する。
     *
     * @param videoId 動画 ID。
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun videoNotes(
        @Argument videoId: String,
        @Argument learnerId: String,
    ): List<VideoNote> = learningStubService.videoNotes(videoId, learnerId)

    /**
     * 指定動画に紐づくクイズ一覧を取得する。
     *
     * @param videoId 動画 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun quizzes(@Argument videoId: String): List<Quiz> = learningStubService.quizzes(videoId)

    /**
     * ID を指定して単一のクイズ（設問含む）を取得する。
     *
     * @param id クイズ ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun quiz(@Argument id: String): Quiz = learningStubService.quiz(id)

    /**
     * 指定学習者のクイズ受験履歴一覧を取得する。
     *
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun myQuizAttempts(@Argument learnerId: String): List<QuizAttempt> =
        learningStubService.myQuizAttempts(learnerId)

    /**
     * 指定学習者が取得した修了証明書一覧を取得する。
     *
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun myCertificates(@Argument learnerId: String): List<Certificate> =
        learningStubService.myCertificates(learnerId)

    /**
     * 組織で利用可能な SaaS モジュール一覧（有効/無効状態含む）を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun saasModules(): List<SaasModule> = saasService.saasModules()

    /**
     * DX 推進イニシアチブ（デジタル化施策）一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun dxInitiatives(): List<DxInitiative> = saasService.dxInitiatives()

    /**
     * CRM 取引先・見込み客（コンタクト）一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun crmContacts(): List<CrmContact> = saasService.crmContacts()

    /**
     * 指定コンタクトに紐づく CRM やり取り履歴（電話・メール等）を取得する。
     *
     * @param contactId CRM コンタクト ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun crmInteractions(@Argument contactId: String): List<CrmInteraction> =
        saasService.crmInteractions(contactId)

    /**
     * 組織の勤怠打刻記録一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun attendanceRecords(): List<AttendanceRecord> = saasService.attendanceRecords()

    /**
     * 組織の休暇申請一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun leaveRequests(): List<LeaveRequest> = saasService.leaveRequests()

    /**
     * 契約書テンプレート一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun contractTemplates(): List<ContractTemplate> = saasService.contractTemplates()

    /**
     * 組織の契約書（下書き・署名済み含む）一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun contracts(): List<Contract> = saasService.contracts()

    /**
     * AI 相談スレッド一覧を取得する。
     *
     * 認証: JWT 必須。未認証時は UNAUTHORIZED エラー。
     * OWNER / ADMIN ロールは組織全体のスレッドを、それ以外は自分のスレッドのみ参照可能。
     */
    @QueryMapping
    fun consultThreads(): List<ConsultThread> = consultService.listThreads()

    /**
     * ID を指定して単一の AI 相談スレッド（メッセージ履歴含む）を取得する。
     *
     * @param id 相談スレッド ID。
     * 認証: JWT 必須。未認証時は UNAUTHORIZED エラー。
     * アクセス権のないスレッドは BAD_REQUEST（not found or access denied）。
     */
    @QueryMapping
    fun consultThread(@Argument id: String): ConsultThread = consultService.getThread(id)

    /**
     * 組織に登録された RAG（検索拡張生成）用社内文書一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun ragDocuments(): List<RagDocument> = consultService.listRagDocuments()

    /**
     * 社内文書をキーワード検索し、関連スニペットのヒット一覧を返す。
     *
     * @param query 検索クエリ文字列。
     * @param limit 返却する最大件数。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun ragSearch(@Argument query: String, @Argument limit: Int): List<RagSearchHit> =
        consultService.searchRag(query, limit)

    /**
     * RAG 検索結果をもとに、社内文書に基づく回答テキストを生成して返す。
     * OpenAI 連携が有効な場合は LLM による要約回答、未設定時はヒット抜粋の整形結果。
     *
     * @param query 質問文。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun ragAnswer(@Argument query: String): RagAnswer = consultService.ragAnswer(query)

    /**
     * 建設プロジェクト（工事案件）一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun constructionProjects(): List<ConstructionProject> = constructionService.listProjects()

    /**
     * プロジェクトに紐づく SaaS モジュール別業務レコード一覧を取得する。
     *
     * @param moduleCode SaaS モジュール種別で絞り込み。`null` の場合は全モジュール。
     * @param projectId プロジェクト ID で絞り込み。`null` の場合は全プロジェクト。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun projectModuleRecords(
        @Argument moduleCode: SaasModuleCode?,
        @Argument projectId: String?,
    ): List<ProjectModuleRecord> = constructionService.listModuleRecords(moduleCode, projectId)

    /**
     * ANDPAD 横断分析ダッシュボード（案件・予算・原価・請求の KPI）を取得する。
     *
     * @param periodDays 集計対象の日数。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun andpadAnalytics(@Argument periodDays: Int): AndpadAnalyticsDashboard =
        extendedService.andpadAnalytics(periodDays)

    /**
     * 外部 API 連携設定一覧を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun apiIntegrations(): List<ApiIntegration> = extendedService.listApiIntegrations()

    /**
     * 指定プロジェクトに紐づく BIM モデル一覧を取得する。
     *
     * @param projectId 建設プロジェクト ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun bimModels(@Argument projectId: String): List<BimModel> =
        extendedService.listBimModels(projectId)

    /**
     * ID を指定して単一の BIM モデル情報を取得する。
     *
     * @param id BIM モデル ID。存在しない場合は BAD_REQUEST。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun bimModel(@Argument id: String): BimModel = extendedService.getBimModel(id)

    /**
     * プロジェクト予算（実行予算・見積等）一覧を条件付きで取得する。
     *
     * @param projectId プロジェクト ID で絞り込み。`null` の場合は全プロジェクト。
     * @param budgetType 予算種別で絞り込み。`null` の場合は全種別。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun projectBudgets(
        @Argument projectId: String?,
        @Argument budgetType: BudgetType?,
    ): List<ProjectBudget> = budgetService.listBudgets(projectId, budgetType)

    /**
     * 指定予算に紐づく費目（予算明細行）一覧を取得する。
     *
     * @param budgetId プロジェクト予算 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun budgetLineItems(@Argument budgetId: String): List<BudgetLineItem> =
        budgetService.listLineItems(budgetId)

    /**
     * 原価エントリ（実績コスト）一覧を条件付きで取得する。
     *
     * @param projectId プロジェクト ID で絞り込み。`null` の場合は全プロジェクト。
     * @param lineItemId 費目 ID で絞り込み。`null` の場合は全費目。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun costEntries(
        @Argument projectId: String?,
        @Argument lineItemId: String?,
    ): List<CostEntry> = budgetService.listCostEntries(projectId, lineItemId)

    /**
     * 指定プロジェクトの予算ダッシュボード（予算 vs 原価の集計ビュー）を取得する。
     *
     * @param projectId 建設プロジェクト ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun budgetDashboard(@Argument projectId: String): BudgetDashboard =
        budgetService.budgetDashboard(projectId)

    /**
     * 全プロジェクトの予算サマリー（一覧用の要約情報）を取得する。
     *
     * 認証: 任意（未認証時はデモテナント）。
     */
    @QueryMapping
    fun projectBudgetSummaries(): List<ProjectBudgetSummary> = budgetService.listBudgetSummaries()
}
