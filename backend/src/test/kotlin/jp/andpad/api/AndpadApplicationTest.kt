package jp.andpad.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class AndpadApplicationTest : AbstractIntegrationTest() {

    @Test
    fun contextLoads() {
        assertThat(mockMvc).isNotNull()
    }

}
