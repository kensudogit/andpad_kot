package jp.andpad.api.domain

/**
 * ANDPAD 建設プロジェクト管理（PM）における拡張機能ドメインモデルを集約するコンテナ。
 *
 * 分析ダッシュボード、外部 API 連携、BIM モデル、AI 相談（チャットボット）、
 * ドキュメント RAG（検索拡張生成）など、コア PM 機能を補完する SaaS モジュール向けの型定義を提供する。
 */
object ExtendedTypes {

    /**
     * 分析ダッシュボード上の KPI（重要業績評価指標）1 件を表す。
     *
     * @property label KPI の表示名（例: 「アクティブ案件数」）
     * @property value KPI の数値
     * @property unit 値の単位（例: 「件」「円」「%」）
     * @property trendPct 前期比の増減率（%）。比較データがない場合は null
     */
    data class AndpadAnalyticsKpi(val label: String, val value: Double, val unit: String, val trendPct: Double?)

    /**
     * 工事案件ステータス別の件数集計を表す。
     *
     * @property status 工事案件のライフサイクルステータス
     * @property count 該当ステータスに属する案件数
     */
    data class ProjectStatusCount(val status: ConstructionProjectStatus, val count: Int)

    /**
     * SaaS モジュール別の利用状況メトリクスを表す。
     *
     * @property moduleCode モジュール識別コード
     * @property moduleName モジュールの表示名
     * @property recordCount 当該モジュールに登録されたレコード件数
     */
    data class ModuleUsageMetric(val moduleCode: SaasModuleCode, val moduleName: String, val recordCount: Int)

    /**
     * ANDPAD 分析ダッシュボードの集約ビューを表す。
     *
     * 組織横断の KPI、案件ステータス分布、モジュール利用状況、予算・原価サマリなどを
     * 経営層・現場管理者向けに一覧化したドメインモデル。
     *
     * @property periodDays 集計対象期間（日数）
     * @property kpis 主要 KPI の一覧
     * @property projectsByStatus ステータス別案件件数
     * @property moduleUsage モジュール別利用メトリクス
     * @property billingTotal 請求合計額（円）
     * @property activeProjects アクティブ（進行中）案件数
     * @property recordsByWeek 週次レコード登録件数の推移
     * @property projectHealthScore 案件健全性スコア（0〜100）
     * @property budgetTotal 予算合計額（円）
     * @property costTotal 原価合計額（円）
     * @property budgetVariancePct 予算対実績の差異率（%）
     * @property costByMonth 月次原価推移
     * @property generatedAt ダッシュボード生成日時（ISO 8601 形式文字列）
     */
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

    /**
     * 外部システムとの API 連携設定を表す。
     *
     * 会計・勤怠・BIM 等の外部サービスとのデータ同期設定を管理する。
     *
     * @property id 連携設定の一意識別子
     * @property name 連携設定の表示名
     * @property provider 連携先プロバイダ名（例: 「freee」「kintone」）
     * @property endpointUrl 連携先 API のエンドポイント URL
     * @property apiKeyHint API キーの末尾数桁など、マスク済みヒント
     * @property status 連携状態（例: 「active」「error」「paused」）
     * @property lastSyncAt 最終同期日時（ISO 8601 形式文字列）
     * @property createdAt 連携設定の作成日時（ISO 8601 形式文字列）
     */
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

    /**
     * 工事案件に紐づく BIM（Building Information Modeling）3D モデルを表す。
     *
     * IFC 等の形式でアップロードされた設計・施工モデルのメタデータとビューア情報を保持する。
     *
     * @property id モデルの一意識別子
     * @property projectId 紐づく工事案件 ID
     * @property projectName 紐づく工事案件名
     * @property title モデルのタイトル
     * @property format ファイル形式（例: 「IFC」「Revit」）
     * @property viewerUrl 3D ビューアの URL
     * @property fileSizeMb ファイルサイズ（MB）。不明な場合は null
     * @property status 処理状態（例: 「ready」「processing」「failed」）
     * @property uploadedBy アップロードしたユーザー名
     * @property createdAt アップロード日時（ISO 8601 形式文字列）
     */
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

    /**
     * AI 相談スレッド内の 1 件のメッセージを表す。
     *
     * @property id メッセージの一意識別子
     * @property role 発言者ロール（例: 「user」「assistant」）
     * @property content メッセージ本文
     * @property createdAt 送信日時（ISO 8601 形式文字列）
     */
    data class ConsultMessage(val id: String, val role: String, val content: String, val createdAt: String)

    /**
     * AI 相談（チャットボット）の会話スレッドを表す。
     *
     * 現場の施工管理・安全・品質に関する問い合わせをスレッド単位で管理する。
     *
     * @property id スレッドの一意識別子
     * @property title スレッドのタイトル（最初の質問内容など）
     * @property createdAt スレッド作成日時（ISO 8601 形式文字列）
     * @property messages スレッド内のメッセージ一覧（時系列順）
     */
    data class ConsultThread(val id: String, val title: String, val createdAt: String, val messages: List<ConsultMessage>)

    /**
     * AI 相談における 1 往復（ユーザー質問とアシスタント回答）の応答を表す。
     *
     * @property threadId 対象スレッド ID
     * @property userMessage ユーザーからの質問メッセージ
     * @property assistantMessage AI アシスタントからの回答メッセージ
     */
    data class ConsultMessageReply(
        val threadId: String,
        val userMessage: ConsultMessage,
        val assistantMessage: ConsultMessage,
    )

    /**
     * RAG（Retrieval-Augmented Generation）用の参照ドキュメントを表す。
     *
     * 施工マニュアル、安全基準、社内規程など、AI 回答の根拠となる文書を保持する。
     *
     * @property id ドキュメントの一意識別子
     * @property title ドキュメントタイトル
     * @property content ドキュメント本文（全文またはチャンク）
     * @property tags 分類タグ（例: 「安全」「品質」「契約」）
     * @property createdAt 登録日時（ISO 8601 形式文字列）
     */
    data class RagDocument(val id: String, val title: String, val content: String, val tags: List<String>, val createdAt: String)

    /**
     * RAG ドキュメント検索の 1 件のヒット結果を表す。
     *
     * @property documentId ヒットしたドキュメント ID
     * @property title ドキュメントタイトル
     * @property snippet クエリに関連する抜粋テキスト
     * @property score 関連度スコア（高いほど関連性が高い）
     */
    data class RagSearchHit(val documentId: String, val title: String, val snippet: String, val score: Double)

    /**
     * RAG による質問応答の結果を表す。
     *
     * @property answer AI が生成した回答文
     * @property sources 回答の根拠となった参照ドキュメントの検索ヒット一覧
     */
    data class RagAnswer(val answer: String, val sources: List<RagSearchHit>)
}
