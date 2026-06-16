/**
 * OpenAI 連携の Spring 設定。
 * [jp.andpad.api.openai.OpenAiProperties] を ConfigurationProperties として有効化し、
 * [jp.andpad.api.openai.OpenAiClient] が API キーとモデル名を注入できるようにする。
 */
package jp.andpad.api.config

import jp.andpad.api.openai.OpenAiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * OpenAI 関連プロパティのバインドを有効にする空の設定クラス。
 * Bean 本体は [OpenAiClient] コンポーネントが担う。
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class OpenAiConfig
