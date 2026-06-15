package jp.andpad.api.service

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var target: AuthService

    @Test
    fun beanLoads() {
        assertThat(target).isNotNull()
    }

}
