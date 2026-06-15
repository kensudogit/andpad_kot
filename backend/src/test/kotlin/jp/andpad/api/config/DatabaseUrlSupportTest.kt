package jp.andpad.api.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DatabaseUrlSupportTest {

    @Test
    fun railwayInternalUsesSslDisable() {
        val raw = "postgresql://postgres:secret@postgres.railway.internal:5432/railway"
        val jdbc = DatabaseUrlSupport.normalizeForJdbc(raw)
        assertThat(jdbc).contains("sslmode=disable")
        assertThat(jdbc).startsWith("jdbc:postgresql://")
        assertThat(jdbc).doesNotContain("secret")
    }

    @Test
    fun publicRailwayHostUsesSslRequire() {
        val raw = "postgresql://postgres:secret@containers-us-west-123.railway.app:6543/railway"
        val jdbc = DatabaseUrlSupport.normalizeForJdbc(raw)
        assertThat(jdbc).contains("sslmode=require")
    }

    @Test
    fun postgresSchemeIsNormalized() {
        val raw = "postgres://postgres:secret@postgres.railway.internal:5432/railway"
        val jdbc = DatabaseUrlSupport.normalizeForJdbc(raw)
        assertThat(jdbc).startsWith("jdbc:postgresql://postgres.railway.internal:5432/railway")
        assertThat(jdbc).contains("sslmode=disable")
    }

    @Test
    fun credentialsAreStrippedFromJdbcUrl() {
        val raw = "postgresql://postgres:secret@postgres.railway.internal:5432/railway"
        val jdbc = DatabaseUrlSupport.normalizeForJdbc(raw)
        assertThat(jdbc).isEqualTo(
                "jdbc:postgresql://postgres.railway.internal:5432/railway?sslmode=disable")
        assertThat(jdbc).doesNotContain("secret")
    }
}
