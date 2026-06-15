package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AuthRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var authRepository: jp.andpad.api.repository.AuthRepository

    @Test
    fun findsDemoUser() {
        assertThat(authRepository.findUserByEmail("demo@sakura-dental.jp")).isPresent()
    }

}
