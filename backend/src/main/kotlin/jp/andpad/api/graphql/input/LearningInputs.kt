package jp.andpad.api.graphql.input

/**
 * 学習・拡張機能・RAG 関連 Mutation の入力型を集約する名前空間。
 *
 * GraphQL スキーマ上は個別の Input 型として公開され、
 * 本オブジェクト内の data class がそれぞれ対応する Mutation の引数となる。
 */
object LearningInputs {

    /**
     * 動画視聴進捗更新 Mutation（`updateWatchProgress`）の入力型。
     */
    data class UpdateWatchProgressInput(
        /** 対象動画 ID。 */
        val videoId: String,
        /** 学習者（ユーザー）ID。 */
        val learnerId: String,
        /** 最後に視聴した再生位置（秒）。 */
        val positionSec: Int,
        /** 視聴完了フラグ。`null` の場合は完了状態を変更しない。 */
        val completed: Boolean?,
    )

    /**
     * 動画メモ新規作成 Mutation（`createVideoNote`）の入力型。
     */
    data class CreateVideoNoteInput(
        /** 対象動画 ID。 */
        val videoId: String,
        /** 学習者（ユーザー）ID。 */
        val learnerId: String,
        /** メモを付けた動画内のタイムスタンプ（秒）。 */
        val timestampSec: Int,
        /** メモ本文。 */
        val body: String,
    )

    /**
     * クイズ回答提出 Mutation（`submitQuizAttempt`）の入力型。
     */
    data class SubmitQuizAttemptInput(
        /** 対象クイズ ID。 */
        val quizId: String,
        /** 学習者（ユーザー）ID。 */
        val learnerId: String,
        /** 各設問に対する選択肢インデックス（0 始まり）のリスト。 */
        val answers: List<Int>,
    )

    /**
     * RAG 社内文書登録 Mutation（`createRagDocument`）の入力型。
     */
    data class CreateRagDocumentInput(
        /** 文書タイトル（検索結果・引用表示に使用）。 */
        val title: String,
        /** 文書本文（RAG インデックスのソーステキスト）。 */
        val content: String,
        /** 分類・検索用タグ一覧。 */
        val tags: List<String>,
    )

    /**
     * 外部 API 連携新規作成 Mutation（`createApiIntegration`）の入力型。
     */
    data class CreateApiIntegrationInput(
        /** 連携設定の表示名称。 */
        val name: String,
        /** 連携先プロバイダ名（例: kintone, freee）。 */
        val provider: String,
        /** API エンドポイント URL。 */
        val endpointUrl: String,
        /** 登録済み API キーの末尾数桁等、UI 表示用ヒント（秘密鍵本体は含まない）。 */
        val apiKeyHint: String,
    )

    /**
     * BIM モデル新規登録 Mutation（`createBimModel`）の入力型。
     */
    data class CreateBimModelInput(
        /** 紐付け先の建設プロジェクト ID。 */
        val projectId: String,
        /** BIM モデルの表示タイトル。 */
        val title: String,
        /** ファイル形式（例: ifc, revit）。 */
        val format: String,
        /** 3D ビューアでモデルを表示する URL。 */
        val viewerUrl: String,
        /** ファイルサイズ（MB）。未設定の場合は `null`。 */
        val fileSizeMb: Double?,
        /** アップロード担当者名。 */
        val uploadedBy: String,
    )
}
