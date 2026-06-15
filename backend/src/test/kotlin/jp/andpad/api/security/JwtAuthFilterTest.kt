package jp.andpad.api.security

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class JwtAuthFilterTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var jwtAuthFilter: JwtAuthFilter

    @Test
    fun filterBeanLoads() {
        assertThat(jwtAuthFilter).isNotNull()
    }

}
