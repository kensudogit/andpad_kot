package jp.andpad.api.util

import java.sql.ResultSet

/** JDBC ResultSet から非 null String を取得するヘルパー。 */
fun ResultSet.requireStr(column: String): String = getString(column) ?: ""

/** 空文字を null に正規化。 */
fun emptyToNull(value: String?): String? = if (value.isNullOrBlank()) null else value

/** PostgreSQL 配列カラムを文字列リストに変換。 */
fun readStringTags(rs: ResultSet, column: String = "tags"): List<String> {
    val sqlArray = rs.getArray(column) ?: return emptyList()
    val raw = sqlArray.array ?: return emptyList()
    @Suppress("UNCHECKED_CAST")
    return when (raw) {
        is Array<*> -> raw.filterIsInstance<String>()
        else -> emptyList()
    }
}
