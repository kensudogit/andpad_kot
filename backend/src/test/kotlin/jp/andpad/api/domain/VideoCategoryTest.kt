package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoCategoryTest {

    @Test
    fun valuesAreDefined() {
        assertThat(VideoCategory.entries).isNotEmpty()
    }

}
