package jp.andpad.api.domain

/**
 * e ラーニング動画のカテゴリ（工種・専門分野）を表す列挙型。
 *
 * 建設現場における専門工種・技術分野ごとに学習コンテンツを分類する。
 * 技能向上研修や資格取得支援のカリキュラム構成に使用する。
 */
enum class VideoCategory {
    /** 歯内療法（専門工種研修カテゴリ） */
    ENDODONTICS,

    /** 歯周病学（専門工種研修カテゴリ） */
    PERIODONTICS,

    /** 補綴学（専門工種研修カテゴリ） */
    PROSTHODONTICS,

    /** 口腔外科（専門工種研修カテゴリ） */
    ORAL_SURGERY,

    /** インプラント（専門工種研修カテゴリ） */
    IMPLANT,

    /** 矯正学（専門工種研修カテゴリ） */
    ORTHODONTICS,

    /** 小児歯科（専門工種研修カテゴリ） */
    PEDIATRIC,

    /** 放射線学（専門工種研修カテゴリ） */
    RADIOLOGY,

    /** 感染管理（現場安全管理・衛生管理） */
    INFECTION_CONTROL,

    /** コミュニケーション（現場連携・報告力向上） */
    COMMUNICATION
}
