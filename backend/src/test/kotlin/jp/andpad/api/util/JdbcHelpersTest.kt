package jp.andpad.api.util

import java.sql.Array as SqlArray
import java.sql.ResultSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class JdbcHelpersTest {

    @Test
    fun emptyToNullNormalizesBlankValues() {
        assertThat(emptyToNull(null)).isNull()
        assertThat(emptyToNull("")).isNull()
        assertThat(emptyToNull("   ")).isNull()
        assertThat(emptyToNull("value")).isEqualTo("value")
    }

    @Test
    fun requireStrReturnsEmptyStringForSqlNull() {
        val rs = mock(ResultSet::class.java)
        `when`(rs.getString("name")).thenReturn(null)

        assertThat(rs.requireStr("name")).isEmpty()
    }

    @Test
    fun requireStrReturnsColumnValue() {
        val rs = mock(ResultSet::class.java)
        `when`(rs.getString("name")).thenReturn("ANDPAD")

        assertThat(rs.requireStr("name")).isEqualTo("ANDPAD")
    }

    @Test
    fun readStringTagsReturnsEmptyListWhenArrayMissing() {
        val rs = mock(ResultSet::class.java)
        `when`(rs.getArray("tags")).thenReturn(null)

        assertThat(readStringTags(rs)).isEmpty()
    }

    @Test
    fun readStringTagsReadsPostgresTextArray() {
        val rs = mock(ResultSet::class.java)
        val sqlArray = mock(SqlArray::class.java)
        `when`(rs.getArray("tags")).thenReturn(sqlArray)
        `when`(sqlArray.array).thenReturn(arrayOf("safety", "quality"))

        assertThat(readStringTags(rs)).containsExactly("safety", "quality")
    }
}
