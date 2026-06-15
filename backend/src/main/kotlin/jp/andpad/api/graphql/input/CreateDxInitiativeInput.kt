package jp.andpad.api.graphql.input

data class CreateDxInitiativeInput(
    val title: String,
    val description: String,
    val status: String,
    val progressPct: Int?,
    val ownerName: String,
    val dueDate: String,
)
