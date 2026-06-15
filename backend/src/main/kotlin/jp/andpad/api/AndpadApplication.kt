package jp.andpad.api

import jp.andpad.api.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AndpadApplication

fun main(args: Array<String>) {
    runApplication<AndpadApplication>(*args)
}
