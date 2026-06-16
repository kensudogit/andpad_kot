/**
 * OpenAI 連携の設定プロパティ。
 * application.yml の `app.openai.*` をバインドし、
 * API キーとモデル名を [OpenAiClient] へ注入する。
 * 環境変数 OPENAI_API_KEY との併用は application.yml 側で設定する。
 */
package jp.andpad.api.openai

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * OpenAI API 接続設定。
 *
 * @property apiKey API キー（null 可、未設定時はローカル RAG フォールバック）
 * @property model モデル ID（null 可、未設定時は gpt-4o-mini）
 */
@ConfigurationProperties(prefix = "app.openai")
data class OpenAiProperties(val apiKey: String?, val model: String?)
