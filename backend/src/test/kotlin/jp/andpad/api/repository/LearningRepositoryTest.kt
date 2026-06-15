package jp.andpad.api.repository

import jp.andpad.api.AbstractIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class LearningRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var learningRepository: jp.andpad.api.repository.LearningRepository

    @Test
    fun listsFeaturedVideos() {
        assertThat(learningRepository.featuredVideos("org_demo")).isNotEmpty()
    }

}
