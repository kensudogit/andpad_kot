package jp.andpad.api.service

import jp.andpad.api.domain.ExtendedTypes.ConsultMessage
import jp.andpad.api.domain.ExtendedTypes.ConsultMessageReply
import jp.andpad.api.domain.ExtendedTypes.ConsultThread
import jp.andpad.api.domain.ExtendedTypes.RagAnswer
import jp.andpad.api.domain.ExtendedTypes.RagDocument
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput
import jp.andpad.api.openai.OpenAiChatMessage
import jp.andpad.api.openai.OpenAiClient
import jp.andpad.api.repository.ConsultRagRepository
import jp.andpad.api.security.AuthPrincipal
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.RagHelper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import kotlin.math.min

/**
 * AI 相談チャット・RAG 検索サービス。
 *
 * **責務**: 相談スレッド管理、OpenAI による回答生成、RAG 文書 CRUD・検索・回答。
 * OWNER/ADMIN は組織全体のスレッドを閲覧可能。
 *
 * **テナント分離**: orgId は TenantContext から取得。スレッドアクセスは orgWide フラグで制御。
 * RAG 検索・文書は org_id スコープ。
 */
@Service
class ConsultService(
    private val consultRagRepository: ConsultRagRepository,
    private val openAiClient: OpenAiClient,
) {

    /** 相談スレッド一覧（ロールに応じて org 全体 or 自分のみ）。 */
    fun listThreads(): List<ConsultThread> {
        val p = TenantContext.requirePrincipal()
        return consultRagRepository.listThreads(p.orgId, p.userId, isOrgWide(p))
    }

    /**
     * スレッド 1 件取得（メッセージ付き）。
     *
     * @throws IllegalArgumentException アクセス拒否または不存在
     */
    fun getThread(id: String): ConsultThread {
        val p = TenantContext.requirePrincipal()
        return consultRagRepository.getThread(p.orgId, p.userId, id, isOrgWide(p))
            ?: throw IllegalArgumentException("thread not found or access denied")
    }

    /**
     * 相談メッセージ送信（threadId 省略時は新規スレッド作成）。
     *
     * OpenAI 有効時は履歴付きで回答生成。利用トークン数を usage_counters に加算。
     *
     * @throws IllegalArgumentException スレッドアクセス拒否
     */
    fun sendMessage(threadId: String?, message: String): ConsultMessageReply {
        val p = TenantContext.requirePrincipal()
        val orgId = p.orgId
        val userId = p.userId
        val orgWide = isOrgWide(p)
        val tid = when {
            threadId.isNullOrBlank() -> {
                var title = truncate(message, 40)
                if (title.isBlank()) {
                    title = "相談スレッド"
                }
                consultRagRepository.createThread(orgId, userId, title).id
            }
            consultRagRepository.verifyThreadAccess(orgId, userId, threadId, orgWide) -> threadId
            else -> throw IllegalArgumentException("thread not found or access denied")
        }
        val historyBefore = consultRagRepository.listMessages(orgId, tid)
        val userMessage = consultRagRepository.addMessage(orgId, tid, "user", message)
        val reply = generateReply(message, historyBefore)
        val assistantMessage = consultRagRepository.addMessage(orgId, tid, "assistant", reply)
        consultRagRepository.incrementConsultUsage(orgId, message.length + reply.length)
        return ConsultMessageReply(tid, userMessage, assistantMessage)
    }

    /** RAG 文書一覧。 */
    fun listRagDocuments(): List<RagDocument> {
        return consultRagRepository.listRagDocuments(TenantContext.orgId())
    }

    /** RAG 文書作成。 */
    fun createRagDocument(input: CreateRagDocumentInput): RagDocument {
        return consultRagRepository.createRagDocument(TenantContext.orgId(), input)
    }

    /**
     * RAG 文書検索（DB 全文検索 → 0 件時はメモリ内ローカル検索）。
     */
    fun searchRag(query: String, limit: Int): List<RagSearchHit> {
        val orgId = TenantContext.orgId()
        val hits = consultRagRepository.searchRagDocuments(orgId, query, limit)
        if (hits.isNotEmpty()) {
            return hits
        }
        return RagHelper.localSearch(consultRagRepository.listRagDocuments(orgId), query, limit)
    }

    /**
     * RAG 検索結果を OpenAI で要約回答（失敗時はスニペット連結フォールバック）。
     */
    fun ragAnswer(query: String): RagAnswer {
        val hits = searchRag(query, 5)
        if (hits.isEmpty()) {
            return RagAnswer(RagHelper.formatHitsAsAnswer(hits), hits)
        }
        if (openAiClient.isEnabled()) {
            try {
                val context = StringBuilder()
                for (i in 0 until min(3, hits.size)) {
                    val h = hits[i]
                    context.append("【").append(h.title).append("】\n").append(h.snippet).append("\n\n")
                }
                val prompt = "以下の社内文書抜粋を参考に、ユーザの質問に日本語で簡潔に答えてください。\n\n" +
                    context +
                    "\n質問: " +
                    query
                val answer = openAiClient.chat(
                    "You summarize internal construction documents for project managers. Cite document titles when helpful.",
                    listOf(),
                    prompt,
                )
                if (StringUtils.hasText(answer)) {
                    return RagAnswer(answer.trim(), hits)
                }
            } catch (ex: Exception) {
                log.warn("RAG OpenAI answer failed: {}", ex.message)
            }
        }
        return RagAnswer(RagHelper.formatHitsAsAnswer(hits), hits)
    }

    /** 相談メッセージに対する AI 回答を生成（OpenAI 未設定時はデモ応答）。 */
    private fun generateReply(message: String, history: List<ConsultMessage>): String {
        var topic = truncate(message, 40)
        if (topic.isBlank()) {
            topic = "（質問内容）"
        }
        if (openAiClient.isEnabled()) {
            try {
                val chatHistory = toChatHistory(history)
                val answer = openAiClient.chat(OpenAiClient.CONSTRUCTION_CONSULT_SYSTEM, chatHistory, message)
                if (StringUtils.hasText(answer)) {
                    return answer.trim()
                }
            } catch (ex: Exception) {
                log.warn("Consult OpenAI chat failed: {}", ex.message)
                return replyOpenAiError(topic, ex.message)
            }
        }
        return replyWithoutOpenAi(topic)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConsultService::class.java)

        /** ConsultMessage リストを OpenAI チャット履歴形式に変換。 */
        private fun toChatHistory(history: List<ConsultMessage>?): List<OpenAiChatMessage> {
            val out = ArrayList<OpenAiChatMessage>()
            if (history == null) {
                return out
            }
            for (msg in history) {
                if (msg == null || !StringUtils.hasText(msg.content)) {
                    continue
                }
                var role = if (msg.role == null) "user" else msg.role.trim().lowercase()
                if ("user" != role && "assistant" != role) {
                    role = "user"
                }
                out.add(OpenAiChatMessage(role, msg.content))
            }
            return out
        }

        /** OpenAI 未設定時のデモ応答文。 */
        private fun replyWithoutOpenAi(topic: String): String {
            return """
                現在 OpenAI 連携（OPENAI_API_KEY）が未設定のため、AI による詳細回答を生成できません。

                Railway の andpad_j サービス → Variables → OPENAI_API_KEY を追加し Redeploy すると、\
                ご質問「%s」に対する本格的な回答が得られます。

                ※ デモ応答として受け付けました。建設現場の安全・工程・品質管理などについてお気軽にご質問ください。""".format(topic)
        }

        /** OpenAI API エラー時のユーザー向けメッセージ。 */
        private fun replyOpenAiError(topic: String, err: String?): String {
            val detail = if (err == null) "unknown error" else truncate(err, 120)
            return """
                OpenAI への問い合わせに失敗しました（%s）。しばらく待ってから再送してください。

                （質問: %s）""".format(detail, topic)
        }

        /** OWNER/ADMIN は組織全体スレッド閲覧可。 */
        private fun isOrgWide(p: AuthPrincipal): Boolean {
            return "OWNER" == p.role || "ADMIN" == p.role
        }

        /** 文字列を最大長で切り詰め。 */
        private fun truncate(s: String?, max: Int): String {
            if (s == null) {
                return ""
            }
            val t = s.trim()
            return if (t.length <= max) t else t.substring(0, max)
        }
    }
}
