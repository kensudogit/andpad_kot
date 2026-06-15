package jp.andpad.api.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubscriptionStatusTest {

    @Test
    fun valuesAreDefined() {
        assertThat(SubscriptionStatus.entries).isNotEmpty()
    }

}
