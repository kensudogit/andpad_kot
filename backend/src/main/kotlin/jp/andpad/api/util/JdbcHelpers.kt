/**
 * JDBC ResultSet 読み取りの拡張ヘルパ。
 * リポジトリ層で NULL 安全な文字列取得、空文字正規化、
 * PostgreSQL 配列型（tags 等）の Kotlin リスト変換を共通化する。
 */
package jp.andpad.api.util

import java.sql.ResultSet

/**
 * 指定カラムの文字列を取得する。SQL NULL は空文字に変換する。
 *
 * @param column カラム名またはラベル
 * @return 非 null 文字列（NULL 時は ""）
 */
fun ResultSet.requireStr(column: String): String = getString(column) ?: ""

/**
 * 空文字・空白のみの文字列を null に正規化する。
 * DB 保存前のオプショナルフィールド整形に使用する。
 *
 * @param value 入力文字列
 * @return 実質的な値、または null
 */
fun emptyToNull(value: String?): String? = if (value.isNullOrBlank()) null else value

/**
 * PostgreSQL の text[] / varchar[] カラムを [List] に変換する。
 *
 * @param rs 現在行を指す ResultSet
 * @param column 配列カラム名（デフォルト: tags）
 * @return 文字列要素のリスト、NULL または非配列時は emptyList()
 */
fun readStringTags(rs: ResultSet, column: String = "tags"): List<String> {
    val sqlArray = rs.getArray(column) ?: return emptyList()
    val raw = sqlArray.array ?: return emptyList()
    @Suppress("UNCHECKED_CAST")
    return when (raw) {
        is Array<*> -> raw.filterIsInstance<String>()
        else -> emptyList()
    }
}
