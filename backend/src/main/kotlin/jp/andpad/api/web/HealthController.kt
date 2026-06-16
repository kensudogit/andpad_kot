/**
 * ヘルスチェック・ランタイム診断 REST コントローラ。
 * ロードバランサー向けの軽量 `/health` と、
 * PostgreSQL 接続・OpenAI 設定・セットアップ状態を返す `/status` を提供する。
 * [RuntimeStatusService] で DB 接続確認を行う。
 */
package jp.andpad.api.web

import jp.andpad.api.domain.Health
import jp.andpad.api.service.RuntimeStatusService
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 運用・監視向けエンドポイント。
 *
 * @property runtimeStatus DB 接続とセットアップ状態の判定
 */
@RestController
class HealthController(private val runtimeStatus: RuntimeStatusService) {

    /**
     * プロセス生存確認用の軽量ヘルスチェック。
     * 認証不要。ロードバランサーの死活監視に使用する。
     *
     * @return サービス名とバージョンを含む [Health]
     */
    @GetMapping("/health")
    fun health(): Health = Health(true, "andpad-api", "2.0.0-saas")

    /**
     * 詳細なランタイム状態を JSON で返す診断エンドポイント。
     * Railway デプロイ後の DB / OpenAI 設定確認に使用する。
     *
     * @return postgres 接続可否、openai キー有無、setup 状態を含む Map
     */
    @GetMapping("/status")
    fun status(): Map<String, Any> {
        val postgres = runtimeStatus.isPostgresConnected()
        return linkedMapOf(
            "service" to "andpad-api",
            "ok" to true,
            "postgres" to postgres,
            "openai" to StringUtils.hasText(System.getenv("OPENAI_API_KEY")), // 環境変数の有無のみ（値は返さない）
            "setup" to runtimeStatus.setupStatus(postgres),
        )
    }
}
