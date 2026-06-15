package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ExtendedRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var extendedRepository: jp.andpad.api.repository.ExtendedRepository

    @Test
    fun buildsAnalytics() {
        assertThat(extendedRepository.andpadAnalytics("org_demo", 30).activeProjects).isGreaterThanOrEqualTo(0)
    }

}
