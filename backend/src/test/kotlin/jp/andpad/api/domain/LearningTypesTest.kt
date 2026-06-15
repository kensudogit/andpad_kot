package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LearningTypesTest {

    @Test
    fun nestedRecordsConstruct() {
        val stats = LearningTypes.DashboardStats(10, 6, 3, 1, 2.5, 4)
        assertThat(stats.videosTotal).isEqualTo(10)
        val video = LearningTypes.Video("v1", "t", "d", VideoCategory.ENDODONTICS, "p",
                SkillLevel.BEGINNER, 60, "thumb", "url", "i1", "inst", listOf(), 0, "2024-01-01", true)
        assertThat(video.id).isEqualTo("v1")
    }

}
