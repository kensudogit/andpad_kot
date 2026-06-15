package jp.andpad.api.service

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ConstructionServiceTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var target: ConstructionService

    @Test
    fun beanLoads() {
        assertThat(target).isNotNull()
    }

}
