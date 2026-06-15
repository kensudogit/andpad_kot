package jp.andpad.api.security

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthPrincipalTest {

    @Test
    fun holdsFields() {
        val p = AuthPrincipal("u1", "org1", "OWNER", "a@b.jp", "Name")
        assertThat(p.userId).isEqualTo("u1")
        assertThat(p.email).isEqualTo("a@b.jp")
    }

}
