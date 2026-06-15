package jp.andpad.api.util

import java.time.Instant
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DatesTest {

    @Test
    fun formatsAndParsesDates() {
        val instant = Instant.parse("2024-01-01T00:00:00Z")
        assertThat(Dates.format(instant)).isEqualTo("2024-01-01T00:00:00Z")
        assertThat(Dates.parseInstant("2024-01-01T00:00:00Z")).isEqualTo(instant)
        val date = LocalDate.of(2024, 6, 1)
        assertThat(Dates.format(date)).isEqualTo("2024-06-01")
        assertThat(Dates.parseDate("2024-06-01")).isEqualTo(date)
    }
}
