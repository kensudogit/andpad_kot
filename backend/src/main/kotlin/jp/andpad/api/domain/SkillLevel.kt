package jp.andpad.api.domain

/**
 * e ラーニングコンテンツの想定スキルレベルを表す列挙型。
 *
 * 動画・学習パスの難易度分類に使用し、学習者が自身の習熟度に合った
 * コンテンツを選択できるようにする。
 */
enum class SkillLevel {
    /** 初級：基本的な施工手順・安全教育向け */
    BEGINNER,

    /** 中級：標準的な現場業務・技術習得向け */
    INTERMEDIATE,

    /** 上級：専門技術・管理業務向け */
    ADVANCED
}
