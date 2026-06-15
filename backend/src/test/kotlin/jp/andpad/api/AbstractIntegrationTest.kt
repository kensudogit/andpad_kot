package jp.andpad.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.testcontainers.containers.PostgreSQLContainer

/** Testcontainers + Spring Boot 統合テスト共通基盤（コンテナは JVM 内で 1 つだけ起動）。 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    companion object {
        @JvmStatic
        val POSTGRES: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("andpad")
            .withUsername("andpad")
            .withPassword("andpad")
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configure(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl)
            registry.add("spring.datasource.username", POSTGRES::getUsername)
            registry.add("spring.datasource.password", POSTGRES::getPassword)
            registry.add("app.jwt.secret") { "test-jwt-secret-at-least-32-characters-long" }
        }
    }

    @Autowired
    protected lateinit var mockMvc: MockMvc

    protected fun loginToken(): String {
        val login: MvcResult = mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"demo@sakura-dental.jp","password":"demo1234"}
                    """.trimIndent(),
                ),
        ).andReturn()
        val body = login.response.contentAsString
        return body.replace(Regex(""".*"token"\s*:\s*"([^"]+)".*"""), "$1")
    }
}
