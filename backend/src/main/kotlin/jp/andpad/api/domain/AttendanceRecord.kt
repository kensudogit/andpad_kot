package jp.andpad.api.domain

data class AttendanceRecord(
    val id: String,
    val userId: String,
    val userName: String,
    val clockIn: String,
    val clockOut: String,
    val note: String,
)
