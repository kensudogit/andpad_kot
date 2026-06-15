package jp.andpad.api.domain

data class DxInitiative(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val progressPct: Int,
    val ownerName: String,
    val dueDate: String,
    val taskCount: Int,
    val tasksDone: Int,
    val createdAt: String,
)
