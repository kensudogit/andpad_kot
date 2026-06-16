/**
 * RAG（Retrieval-Augmented Generation）のローカル検索・スニペット生成ヘルパ。
 * OpenAI 未設定時やオフライン時に、登録文書リストからキーワード一致検索を行い、
 * 相談 API 向けのスニペットとフォールバック回答文を組み立てる。
 */
package jp.andpad.api.util

import java.util.Locale
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit

/**
 * 簡易キーワード RAG 検索と回答フォーマット。
 */
object RagHelper {

    /**
     * 文書リストからクエリ文字列を含むタイトル/本文をスコア付きで検索する。
     * OpenAI Embeddings 未使用のローカルフォールバック実装。
     *
     * @param docs 検索対象文書
     * @param query 検索キーワード（空なら emptyList）
     * @param limit 最大ヒット数（0 以下は 5 件に補正）
     * @return スコア降順相当の [RagSearchHit] リスト
     */
    fun localSearch(docs: List<RagDocument>, query: String?, limit: Int): List<RagSearchHit> {
        val q = query?.trim().orEmpty()
        if (q.isEmpty() || docs.isEmpty()) {
            return emptyList()
        }
        val max = if (limit <= 0) 5 else limit
        val qLower = q.lowercase(Locale.ROOT)
        val out = mutableListOf<RagSearchHit>()
        for (doc in docs) {
            val titleLower = doc.title.lowercase(Locale.ROOT)
            val contentLower = doc.content.lowercase(Locale.ROOT)
            val score = when {
                titleLower.contains(qLower) -> 1.0 // タイトル一致を最優先
                contentLower.contains(qLower) -> 0.7
                else -> continue
            }
            out.add(RagSearchHit(doc.id, doc.title, snippet(doc.content, q, 180), score))
            if (out.size >= max) {
                break
            }
        }
        return out
    }

    /**
     * クエリ周辺の本文抜粋（スニペット）を生成する。
     *
     * @param content 全文
     * @param query ハイライト位置の目安キーワード
     * @param maxChars 最大文字数（0 以下は 180）
     * @return 先頭/末尾に "..." を付けた抜粋
     */
    fun snippet(content: String?, query: String?, maxChars: Int): String {
        if (content.isNullOrBlank()) {
            return ""
        }
        var max = maxChars
        if (max <= 0) {
            max = 180
        }
        val lower = content.lowercase(Locale.ROOT)
        val q = query?.trim()?.lowercase(Locale.ROOT).orEmpty()
        val idx = if (q.isEmpty()) -1 else lower.indexOf(q)
        if (idx < 0) {
            return truncate(content, max)
        }
        // マッチ位置の少し手前から切り出し、読みやすい長さに調整
        val start = maxOf(0, idx - max / 4)
        val end = minOf(content.length, start + max * 2)
        var s = content.substring(start, end)
        s = truncate(s, max)
        return if (start > 0) "...$s" else s
    }

    /**
     * 検索ヒットをユーザー向けプレーンテキスト回答に整形する。
     * OpenAI 未設定時の Consult API フォールバック応答に使用する。
     *
     * @param hits 検索結果（null/空なら「見つかりませんでした」メッセージ）
     * @return 日本語の説明文
     */
    fun formatHitsAsAnswer(hits: List<RagSearchHit>?): String {
        if (hits.isNullOrEmpty()) {
            return "関連する文書が見つかりませんでした。文書を登録するか、別のキーワードで検索してください。"
        }
        val b = StringBuilder("登録文書から見つかった関連情報です。\n\n")
        for (i in 0 until minOf(3, hits.size)) {
            val h = hits[i]
            b.append("【").append(h.title).append("】\n").append(h.snippet).append("\n\n")
        }
        b.append("（OPENAI_API_KEY を設定すると、参照文書に基づく要約回答を生成できます。）")
        return b.toString()
    }

    /**
     * 文字列を最大長で切り詰め、超過分は "..." を付与する。
     *
     * @param s 入力文字列
     * @param max 最大文字数
     * @return 切り詰め後の文字列
     */
    private fun truncate(s: String, max: Int): String {
        if (s.length <= max) {
            return s
        }
        return s.substring(0, max) + "..."
    }
}
