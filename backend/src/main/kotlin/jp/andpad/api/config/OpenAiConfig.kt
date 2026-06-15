package jp.andpad.api.config

import jp.andpad.api.openai.OpenAiProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAiProperties::class)
class OpenAiConfig
