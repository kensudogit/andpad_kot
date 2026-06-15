package jp.andpad.api.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(val jwt: Jwt) {
    data class Jwt(val secret: String, val ttlHours: Int)
}
