package jp.andpad.api.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IdsTest {

    @Test
    fun randomIdHasPrefix() {
        assertThat(Ids.random("test-")).startsWith("test-")
    }

}
