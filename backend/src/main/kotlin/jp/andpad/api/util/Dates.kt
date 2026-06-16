/**
 * 日時・日付のフォーマットとパースユーティリティ。
 * API レスポンスでは ISO-8601  instant 文字列を統一形式で返し、
 * GraphQL / JDBC 層からの文字列入力を [Instant] / [LocalDate] に変換する。
 */
package jp.andpad.api.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * UTC 基準の日時操作ヘルパ。
 *
 * - [ISO]: Instant の ISO-8601 フォーマッタ
 */
object Dates {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    /**
     * Instant を ISO-8601 文字列に変換する。null は null を返す。
     *
     * @param instant 変換対象（null 可）
     * @return フォーマット済み文字列、または null
     */
    fun format(instant: Instant?): String? = instant?.let { ISO.format(it) }

    /**
     * Instant を ISO-8601 文字列に変換する（非 null 前提）。
     *
     * @param instant 変換対象
     * @return フォーマット済み文字列
     */
    fun formatRequired(instant: Instant): String = ISO.format(instant)

    /**
     * Instant をフォーマットする。null の場合は現在時刻（UTC）を使用する。
     *
     * @param instant 変換対象（null 可）
     * @return フォーマット済み文字列
     */
    fun formatRequiredOrNow(instant: Instant?): String =
        if (instant == null) ISO.format(now()) else ISO.format(instant)

    /**
     * LocalDate を ISO 日付文字列（yyyy-MM-dd）に変換する。
     *
     * @param date 変換対象（null 可）
     * @return 日付文字列、または null
     */
    fun format(date: LocalDate?): String? = date?.toString()

    /**
     * ISO-8601 文字列を [Instant] にパースする。
     *
     * @param value 日時文字列（null/空白は null）
     * @return パース結果、無効入力は null
     */
    fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) {
            return null
        }
        return Instant.parse(value)
    }

    /**
     * yyyy-MM-dd 形式の文字列を [LocalDate] にパースする。
     *
     * @param value 日付文字列（null/空白は null）
     * @return パース結果、無効入力は null
     */
    fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) {
            return null
        }
        return LocalDate.parse(value)
    }

    /**
     * 現在時刻を UTC で取得する。
     *
     * @return 現在の [Instant]（UTC 正規化済み）
     */
    fun now(): Instant = Instant.now().atOffset(ZoneOffset.UTC).toInstant()
}
