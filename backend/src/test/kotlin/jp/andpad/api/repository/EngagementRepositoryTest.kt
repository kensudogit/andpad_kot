package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class EngagementRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var engagementRepository: jp.andpad.api.repository.EngagementRepository

    @Test
    fun beanLoads() {
        assertThat(engagementRepository).isNotNull()
    }

}
