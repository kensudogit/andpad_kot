package jp.andpad.api.domain

/**
 * 学習アクティビティイベントの種別を表す列挙型。
 *
 * 学習者の行動ログを分類し、エンゲージメント分析や
 * 通知・監査ログの生成に使用する。
 */
enum class LearningActivityKind {
    /** 動画視聴進捗が更新された */
    PROGRESS_UPDATED,

    /** 動画視聴中にメモが作成された */
    NOTE_CREATED,

    /** 動画のブックマークが追加または解除された */
    BOOKMARK_TOGGLED,

    /** 学習パスに登録（受講開始）された */
    PATH_ENROLLED,

    /** クイズが提出・採点された */
    QUIZ_SUBMITTED
}
