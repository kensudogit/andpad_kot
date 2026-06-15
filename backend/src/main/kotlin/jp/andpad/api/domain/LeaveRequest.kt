package jp.andpad.api.domain

data class LeaveRequest(
    val id: String,
    val userId: String,
    val userName: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String,
    val createdAt: String,
)
