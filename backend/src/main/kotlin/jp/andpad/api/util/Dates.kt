package jp.andpad.api.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Dates {
    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    fun format(instant: Instant?): String? = instant?.let { ISO.format(it) }

    fun formatRequired(instant: Instant): String = ISO.format(instant)

    fun formatRequiredOrNow(instant: Instant?): String =
        if (instant == null) ISO.format(now()) else ISO.format(instant)

    fun format(date: LocalDate?): String? = date?.toString()

    fun parseInstant(value: String?): Instant? {
        if (value.isNullOrBlank()) {
            return null
        }
        return Instant.parse(value)
    }

    fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) {
            return null
        }
        return LocalDate.parse(value)
    }

    fun now(): Instant = Instant.now().atOffset(ZoneOffset.UTC).toInstant()
}
