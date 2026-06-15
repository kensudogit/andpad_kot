package jp.andpad.api.web

import jp.andpad.api.domain.Health
import jp.andpad.api.service.RuntimeStatusService
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(private val runtimeStatus: RuntimeStatusService) {

    @GetMapping("/health")
    fun health(): Health = Health(true, "andpad-api", "2.0.0-saas")

    @GetMapping("/status")
    fun status(): Map<String, Any> {
        val postgres = runtimeStatus.isPostgresConnected()
        return linkedMapOf(
            "service" to "andpad-api",
            "ok" to true,
            "postgres" to postgres,
            "openai" to StringUtils.hasText(System.getenv("OPENAI_API_KEY")),
            "setup" to runtimeStatus.setupStatus(postgres),
        )
    }
}
