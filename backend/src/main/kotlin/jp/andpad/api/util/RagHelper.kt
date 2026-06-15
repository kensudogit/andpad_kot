package jp.andpad.api.util

import java.util.Locale
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit

object RagHelper {

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
                titleLower.contains(qLower) -> 1.0
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
        val start = maxOf(0, idx - max / 4)
        val end = minOf(content.length, start + max * 2)
        var s = content.substring(start, end)
        s = truncate(s, max)
        return if (start > 0) "...$s" else s
    }

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

    private fun truncate(s: String, max: Int): String {
        if (s.length <= max) {
            return s
        }
        return s.substring(0, max) + "..."
    }
}
