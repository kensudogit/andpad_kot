package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ConstructionRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var constructionRepository: jp.andpad.api.repository.ConstructionRepository

    @Test
    fun listsDemoProjects() {
        assertThat(constructionRepository.listProjects("org_demo")).isNotEmpty()
    }

}
