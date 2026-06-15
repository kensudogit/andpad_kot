package jp.andpad.api.security

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class JwtServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var jwtService: JwtService

    @Test
    fun issueAndParseToken() {
        val principal = AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name")
        val token = jwtService.issueToken(principal)
        val parsed = jwtService.parseToken(token)
        assertThat(parsed.userId).isEqualTo("u1")
        assertThat(parsed.orgId).isEqualTo("org1")
    }

}
