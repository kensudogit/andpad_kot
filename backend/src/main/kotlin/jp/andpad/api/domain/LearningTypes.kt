package jp.andpad.api.domain

/**
 * ANDPAD 建設 PM プラットフォームにおける e ラーニング（技能向上・安全教育）ドメインモデルを集約するコンテナ。
 *
 * 施工技術動画、学習パス、クイズ、視聴進捗、修了証、学習分析など、
 * 現場スタッフのスキル開発とコンプライアンス研修を支援する型定義を提供する。
 */
object LearningTypes {

    /**
     * 学習管理ダッシュボードのサマリ統計を表す。
     *
     * @property videosTotal 登録動画の総数
     * @property learningPathsTotal 登録学習パスの総数
     * @property quizzesTotal 登録クイズの総数
     * @property completionsThisMonth 当月の修了・完了件数
     * @property watchHoursThisMonth 当月の総視聴時間（時間）
     * @property activeLearners 当月アクティブな学習者数
     */
    data class DashboardStats(
        val videosTotal: Int,
        val learningPathsTotal: Int,
        val quizzesTotal: Int,
        val completionsThisMonth: Int,
        val watchHoursThisMonth: Double,
        val activeLearners: Int,
    )

    /**
     * 学習コンテンツの講師（インストラクター）情報を表す。
     *
     * @property id 講師の一意識別子
     * @property name 講師名
     * @property title 肩書き（例: 「一級建築士」「安全管理者」）
     * @property specialty 専門分野
     * @property bio 経歴・自己紹介文
     * @property avatarUrl プロフィール画像 URL
     * @property videoCount 担当動画数
     */
    data class Instructor(
        val id: String,
        val name: String,
        val title: String,
        val specialty: String,
        val bio: String,
        val avatarUrl: String,
        val videoCount: Int,
    )

    /**
     * 施工技術・安全教育などの学習動画コンテンツを表す。
     *
     * @property id 動画の一意識別子
     * @property title 動画タイトル
     * @property description 動画の概要説明
     * @property category 動画のカテゴリ（工種・分野）
     * @property procedure 対象する施工手順・工程名
     * @property skillLevel 想定する学習者のスキルレベル
     * @property durationSec 動画の再生時間（秒）
     * @property thumbnailUrl サムネイル画像 URL
     * @property videoUrl 動画ファイルまたはストリーミング URL
     * @property instructorId 担当講師 ID
     * @property instructorName 担当講師名
     * @property tags 検索・分類用タグ一覧
     * @property viewCount 累計視聴回数
     * @property publishedAt 公開日時（ISO 8601 形式文字列）
     * @property featured おすすめ・注目動画として表示するか
     */
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

    /**
     * 複数動画を順序付きで組み合わせた学習パス（カリキュラム）を表す。
     *
     * 特定工種の技能習得や資格取得に向けた体系的な学習コースを定義する。
     *
     * @property id 学習パスの一意識別子
     * @property title 学習パスタイトル
     * @property description 学習パスの概要説明
     * @property category 学習パスのカテゴリ（工種・分野）
     * @property skillLevel 想定する学習者のスキルレベル
     * @property videoIds 含まれる動画 ID の順序付きリスト
     * @property estimatedMinutes 修了までの目安時間（分）
     * @property enrolledCount 登録（受講開始）者数
     * @property certificateTitle 修了時に発行される証書のタイトル
     */
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

    /**
     * 学習者の動画視聴進捗を表す。
     *
     * @property id 進捗レコードの一意識別子
     * @property videoId 対象動画 ID
     * @property learnerId 学習者（ユーザー）ID
     * @property positionSec 最後に視聴した再生位置（秒）
     * @property completed 動画を最後まで視聴完了したか
     * @property updatedAt 進捗更新日時（ISO 8601 形式文字列）
     */
    data class WatchProgress(
        val id: String,
        val videoId: String,
        val learnerId: String,
        val positionSec: Int,
        val completed: Boolean,
        val updatedAt: String,
    )

    /**
     * 動画視聴中に学習者が付けたタイムスタンプ付きメモを表す。
     *
     * @property id メモの一意識別子
     * @property videoId 対象動画 ID
     * @property learnerId 学習者（ユーザー）ID
     * @property timestampSec メモを付けた動画内の位置（秒）
     * @property body メモ本文
     * @property createdAt メモ作成日時（ISO 8601 形式文字列）
     */
    data class VideoNote(
        val id: String,
        val videoId: String,
        val learnerId: String,
        val timestampSec: Int,
        val body: String,
        val createdAt: String,
    )

    /**
     * クイズ問題の選択肢 1 件を表す。
     *
     * @property id 選択肢の一意識別子
     * @property label 選択肢の表示テキスト
     */
    data class QuizChoice(val id: String, val label: String)

    /**
     * クイズの 1 問を表す。
     *
     * @property id 問題の一意識別子
     * @property prompt 問題文
     * @property choices 選択肢一覧
     * @property correctIndex 正解の選択肢インデックス（0 始まり）
     */
    data class QuizQuestion(val id: String, val prompt: String, val choices: List<QuizChoice>, val correctIndex: Int)

    /**
     * 動画に紐づく理解度確認クイズを表す。
     *
     * 安全教育や施工手順の理解を確認するためのテストを定義する。
     *
     * @property id クイズの一意識別子
     * @property videoId 紐づく動画 ID
     * @property title クイズタイトル
     * @property passingScore 合格に必要な最低スコア（%）
     * @property questions クイズ問題一覧
     */
    data class Quiz(
        val id: String,
        val videoId: String,
        val title: String,
        val passingScore: Int,
        val questions: List<QuizQuestion>,
    )

    /**
     * 学習者のクイズ受験結果を表す。
     *
     * @property id 受験レコードの一意識別子
     * @property quizId 受験したクイズ ID
     * @property learnerId 学習者（ユーザー）ID
     * @property score 得点（%）
     * @property passed 合格基準を満たしたか
     * @property completedAt 受験完了日時（ISO 8601 形式文字列）
     */
    data class QuizAttempt(
        val id: String,
        val quizId: String,
        val learnerId: String,
        val score: Int,
        val passed: Boolean,
        val completedAt: String,
    )

    /**
     * 学習者がお気に入り登録した動画ブックマークを表す。
     *
     * @property id ブックマークの一意識別子
     * @property videoId ブックマーク対象の動画 ID
     * @property learnerId 学習者（ユーザー）ID
     * @property createdAt ブックマーク作成日時（ISO 8601 形式文字列）
     */
    data class Bookmark(val id: String, val videoId: String, val learnerId: String, val createdAt: String)

    /**
     * 学習パス修了時に発行される修了証を表す。
     *
     * @property id 修了証の一意識別子
     * @property pathId 修了した学習パス ID
     * @property learnerId 学習者（ユーザー）ID
     * @property title 修了証のタイトル
     * @property issuedAt 発行日時（ISO 8601 形式文字列）
     */
    data class Certificate(val id: String, val pathId: String, val learnerId: String, val title: String, val issuedAt: String)

    /**
     * ページネーション情報を表す。
     *
     * @property total 全件数
     * @property page 現在のページ番号（1 始まり）
     * @property pageSize 1 ページあたりの件数
     * @property totalPages 総ページ数
     */
    data class PageInfo(val total: Int, val page: Int, val pageSize: Int, val totalPages: Int)

    /**
     * 動画一覧のページング結果を表す。
     *
     * @property items 現在ページの動画一覧
     * @property pageInfo ページネーション情報
     */
    data class VideoPage(val items: List<Video>, val pageInfo: PageInfo)

    /**
     * 学習アクティビティのイベントログ 1 件を表す。
     *
     * 学習者の行動（視聴進捗更新、メモ作成、クイズ提出など）を記録する。
     *
     * @property kind イベント種別
     * @property learnerId 学習者（ユーザー）ID
     * @property videoId 関連動画 ID（該当しない場合は空文字）
     * @property pathId 関連学習パス ID（該当しない場合は空文字）
     * @property quizId 関連クイズ ID（該当しない場合は空文字）
     * @property message イベントの説明メッセージ
     * @property occurredAt イベント発生日時（ISO 8601 形式文字列）
     */
    data class LearningActivityEvent(
        val kind: LearningActivityKind,
        val learnerId: String,
        val videoId: String,
        val pathId: String,
        val quizId: String,
        val message: String,
        val occurredAt: String,
    )

    /**
     * 学習分析ダッシュボード上の KPI 1 件を表す。
     *
     * @property label KPI の表示名
     * @property value KPI の数値
     * @property unit 値の単位
     * @property trendPct 前期比の増減率（%）。比較データがない場合は null
     */
    data class AnalyticsKpi(val label: String, val value: Double, val unit: String, val trendPct: Double?)

    /**
     * カテゴリ別の学習コンテンツ件数を表す。
     *
     * @property category 動画カテゴリ（工種・分野）
     * @property count 該当カテゴリの件数
     */
    data class CategoryMetric(val category: VideoCategory, val count: Int)

    /**
     * 動画別の視聴・修了メトリクスを表す。
     *
     * @property videoId 動画 ID
     * @property title 動画タイトル
     * @property views 累計視聴回数
     * @property completions 修了（視聴完了）回数
     */
    data class VideoMetric(val videoId: String, val title: String, val views: Int, val completions: Int)

    /**
     * 学習分析ダッシュボードの集約ビューを表す。
     *
     * 組織内の e ラーニング利用状況、視聴時間推移、人気動画などを可視化する。
     *
     * @property periodDays 集計対象期間（日数）
     * @property kpis 主要 KPI の一覧
     * @property watchHoursByWeek 週次視聴時間（時間）の推移
     * @property completionsByCategory カテゴリ別修了件数
     * @property topVideos 視聴数上位の動画メトリクス
     * @property learnerEngagementScore 学習者エンゲージメントスコア（0〜100）
     */
    data class AnalyticsBoard(
        val periodDays: Int,
        val kpis: List<AnalyticsKpi>,
        val watchHoursByWeek: List<Double>,
        val completionsByCategory: List<CategoryMetric>,
        val topVideos: List<VideoMetric>,
        val learnerEngagementScore: Double,
    )

    /**
     * AI による学習分析インサイト（改善提案）を表す。
     *
     * @property summary 分析結果の要約文
     * @property strengths 学習プログラムの強み・好調な点
     * @property risks リスク・課題点
     * @property recommendations 改善・施策の推奨事項
     * @property generatedAt インサイト生成日時（ISO 8601 形式文字列）
     */
    data class AnalyticsInsight(
        val summary: String,
        val strengths: List<String>,
        val risks: List<String>,
        val recommendations: List<String>,
        val generatedAt: String,
    )
}
