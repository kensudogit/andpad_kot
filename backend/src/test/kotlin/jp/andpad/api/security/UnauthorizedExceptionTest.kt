package jp.andpad.api.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UnauthorizedExceptionTest {

    @Test
    fun carriesMessage() {
        assertThat(UnauthorizedException("denied").message).isEqualTo("denied")
    }

}
