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

/**
 * GraphQL Mutation ルートコントローラー。
 *
 * Spring GraphQL の [MutationMapping] により、GraphQL スキーマの `Mutation` 型フィールドを
 * 各サービス層の書き込み操作へ委譲する。エンドポイントは `/graphql`。
 *
 * 認証は HTTP レベルでは必須ではなく、JWT が付与されている場合のみ実テナント・実ユーザーが
 * 操作対象となる。未認証時はデモテナント（`org_demo` / `user_demo`）へ書き込まれる。
 * 組織更新および AI 相談送信はサービス層で JWT 必須（[jp.andpad.api.security.TenantContext.requirePrincipal]）。
 */
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

    /**
     * 組織プロフィール（名称・スラッグ・席数・タイムゾーン等）を更新する。
     *
     * @param input 更新内容。各フィールドは GraphQL スキーマ上で部分更新に利用可能。
     * 認証: JWT 必須。未認証時は UNAUTHORIZED エラー。
     */
    @MutationMapping
    fun updateOrganization(@Argument input: UpdateOrganizationInput): Organization =
        organizationService.updateOrganization(input)

    /**
     * 指定期間の ANDPAD 横断分析データから、AI 風の分析インサイト（要約・提言）を生成する。
     *
     * @param periodDays 集計対象の日数。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun generateAnalyticsInsight(@Argument periodDays: Int): AnalyticsInsight =
        extendedService.generateAnalyticsInsight(periodDays)

    /**
     * 学習動画の視聴進捗（再生位置・完了フラグ）を更新する。
     *
     * @param input 動画 ID・学習者 ID・再生位置・完了状態。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun updateWatchProgress(@Argument input: UpdateWatchProgressInput): WatchProgress =
        learningStubService.updateWatchProgress(input)

    /**
     * 動画の視聴回数を 1 件インクリメントし、更新後の動画情報を返す。
     *
     * @param videoId 対象動画 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun recordVideoView(@Argument videoId: String): Video =
        learningStubService.recordVideoView(videoId)

    /**
     * 動画の特定タイムスタンプにメモ（ノート）を新規作成する。
     *
     * @param input 動画 ID・学習者 ID・秒位置・本文。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createVideoNote(@Argument input: CreateVideoNoteInput): VideoNote =
        learningStubService.createVideoNote(input)

    /**
     * ID を指定して動画メモを削除する。
     *
     * @param id 削除対象のノート ID。
     * @return 削除成功時 `true`。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun deleteVideoNote(@Argument id: String): Boolean =
        learningStubService.deleteVideoNote(id)

    /**
     * 指定動画のブックマーク状態をトグル（追加/解除）する。
     *
     * @param videoId 動画 ID。
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun toggleBookmark(
        @Argument videoId: String,
        @Argument learnerId: String,
    ): Bookmark = learningStubService.toggleBookmark(videoId, learnerId)

    /**
     * 指定学習者を学習パスに登録（エンロール）する。
     *
     * @param pathId 学習パス ID。
     * @param learnerId 学習者（ユーザー）ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun enrollLearningPath(
        @Argument pathId: String,
        @Argument learnerId: String,
    ): LearningPath = learningStubService.enrollLearningPath(pathId, learnerId)

    /**
     * クイズの回答を提出し、採点結果付きの受験記録を返す。
     *
     * @param input クイズ ID・学習者 ID・選択肢インデックスの回答リスト。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun submitQuizAttempt(@Argument input: SubmitQuizAttemptInput): QuizAttempt =
        learningStubService.submitQuizAttempt(input)

    /**
     * DX 推進イニシアチブ（デジタル化施策）を新規作成する。
     *
     * @param input タイトル・説明・ステータス・進捗率・担当者・期限。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createDxInitiative(@Argument input: CreateDxInitiativeInput): DxInitiative =
        saasService.createDxInitiative(input)

    /**
     * CRM コンタクト（取引先・見込み客）を新規作成する。
     *
     * @param input 氏名・連絡先・会社・ステージ・メモ。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createCrmContact(@Argument input: CreateCrmContactInput): CrmContact =
        saasService.createCrmContact(input)

    /**
     * 指定 CRM コンタクトへのやり取り記録（電話・メール等）を追加する。
     *
     * @param contactId 対象コンタクト ID。
     * @param kind やり取り種別（例: call, email, meeting）。
     * @param summary やり取り内容の要約。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createCrmInteraction(
        @Argument contactId: String,
        @Argument kind: String,
        @Argument summary: String,
    ): CrmInteraction = saasService.createCrmInteraction(contactId, kind, summary)

    /**
     * 現在ユーザーの出勤打刻を記録する。
     *
     * @param note 打刻時の備考。`null` の場合は空文字として記録。
     * 認証: 任意（未認証時はデモユーザー `user_demo` で打刻）。
     */
    @MutationMapping
    fun clockIn(@Argument note: String?): AttendanceRecord = saasService.clockIn(note)

    /**
     * 現在ユーザーの退勤打刻を記録する。
     *
     * 認証: 任意（未認証時はデモユーザー `user_demo` で打刻）。
     */
    @MutationMapping
    fun clockOut(): AttendanceRecord = saasService.clockOut()

    /**
     * 休暇申請を新規作成する。
     *
     * @param input 開始日・終了日・理由。
     * 認証: 任意（未認証時はデモテナント・デモユーザー）。
     */
    @MutationMapping
    fun createLeaveRequest(@Argument input: CreateLeaveRequestInput): LeaveRequest =
        saasService.createLeaveRequest(input)

    /**
     * 指定休暇申請を承認する。
     *
     * @param id 休暇申請 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun approveLeaveRequest(@Argument id: String): LeaveRequest =
        saasService.approveLeaveRequest(id)

    /**
     * 契約書テンプレートを新規作成する。
     *
     * @param name テンプレート名称。
     * @param body テンプレート本文（プレースホルダー可）。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createContractTemplate(
        @Argument name: String,
        @Argument body: String,
    ): ContractTemplate = saasService.createContractTemplate(name, body)

    /**
     * テンプレートをもとに契約書インスタンスを新規作成する。
     *
     * @param input テンプレート ID・タイトル・相手方情報・本文。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createContract(@Argument input: CreateContractInput): Contract =
        saasService.createContract(input)

    /**
     * 指定契約書に電子署名を付与し、署名済み状態に更新する。
     *
     * @param id 契約書 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun signContract(@Argument id: String): Contract = saasService.signContract(id)

    /**
     * AI 相談スレッドへメッセージを送信し、ユーザー発言と AI 応答を返す。
     * 新規スレッド作成と既存スレッドへの追記の両方に対応。
     *
     * @param threadId 既存スレッド ID。`null` または空の場合はメッセージ先頭から新規スレッドを作成。
     * @param message ユーザーからの相談メッセージ本文。
     * 認証: JWT 必須。未認証時は UNAUTHORIZED エラー。
     * 存在しないまたはアクセス権のない threadId は BAD_REQUEST。
     */
    @MutationMapping
    fun sendConsultMessage(
        @Argument threadId: String?,
        @Argument message: String,
    ): ConsultMessageReply = consultService.sendMessage(threadId, message)

    /**
     * RAG 検索用の社内文書を新規登録する。
     *
     * @param input タイトル・本文・タグ一覧。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createRagDocument(@Argument input: CreateRagDocumentInput): RagDocument =
        consultService.createRagDocument(input)

    /**
     * 指定 SaaS モジュールの有効/無効を切り替える。
     *
     * @param code 対象モジュールコード。
     * @param enabled `true` で有効化、`false` で無効化。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun setSaasModuleEnabled(
        @Argument code: SaasModuleCode,
        @Argument enabled: Boolean,
    ): SaasModule = saasService.setModuleEnabled(code, enabled)

    /**
     * 建設プロジェクト（工事案件）を新規作成する。
     *
     * @param input 案件名・現場住所・ステータス・担当者・工期。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createConstructionProject(@Argument input: CreateConstructionProjectInput): ConstructionProject =
        constructionService.createProject(input)

    /**
     * プロジェクトに SaaS モジュール別の業務レコード（請求・安全等）を追加する。
     *
     * @param input プロジェクト ID・モジュール種別・タイトル・詳細等。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createProjectModuleRecord(@Argument input: CreateProjectModuleRecordInput): ProjectModuleRecord =
        constructionService.createModuleRecord(input)

    /**
     * 外部 API 連携設定を新規作成する。
     *
     * @param input 連携名・プロバイダ・エンドポイント URL・API キー表示用ヒント。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createApiIntegration(@Argument input: CreateApiIntegrationInput): ApiIntegration =
        extendedService.createApiIntegration(input)

    /**
     * 指定 API 連携の同期処理を実行し、更新後の連携状態を返す。
     *
     * @param id API 連携 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun syncApiIntegration(@Argument id: String): ApiIntegration =
        extendedService.syncApiIntegration(id)

    /**
     * プロジェクトに BIM モデル情報を新規登録する。
     *
     * @param input プロジェクト ID・タイトル・形式・ビューア URL 等。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createBimModel(@Argument input: CreateBimModelInput): BimModel =
        extendedService.createBimModel(input)

    /**
     * プロジェクト予算（実行予算・見積等）を新規作成する。
     *
     * @param input プロジェクト ID・予算名・種別・ステータス・金額等。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createProjectBudget(@Argument input: CreateProjectBudgetInput): ProjectBudget =
        budgetService.createBudget(input)

    /**
     * 予算に費目（明細行）を追加する。
     *
     * @param input 予算 ID・カテゴリ・WBS・金額・並び順等。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createBudgetLineItem(@Argument input: CreateBudgetLineItemInput): BudgetLineItem =
        budgetService.createLineItem(input)

    /**
     * 原価エントリ（実績コスト）を手動登録する。
     *
     * @param input プロジェクト・費目・種別・業者・金額・日付等。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createCostEntry(@Argument input: CreateCostEntryInput): CostEntry =
        budgetService.createCostEntry(input)

    /**
     * 指定プロジェクト予算を承認済みステータスに更新する。
     *
     * @param id プロジェクト予算 ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun approveProjectBudget(@Argument id: String): ProjectBudget =
        budgetService.approveBudget(id)

    /**
     * 請求レコードから原価エントリを自動生成する（請求→原価連携）。
     *
     * @param billingRecordId 請求レコード ID。
     * @param projectId 紐付け先プロジェクト ID。
     * 認証: 任意（未認証時はデモテナント）。
     */
    @MutationMapping
    fun createCostFromBilling(
        @Argument billingRecordId: String,
        @Argument projectId: String,
    ): CostEntry = budgetService.createCostFromBilling(billingRecordId, projectId)
}
