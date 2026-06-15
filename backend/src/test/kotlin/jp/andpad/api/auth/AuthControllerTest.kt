package jp.andpad.api.auth

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthControllerTest : AbstractIntegrationTest() {

    @Test
    fun loginReturnsToken()  {
        val token = loginToken()
        assertThat(token).isNotBlank()
    }

}
