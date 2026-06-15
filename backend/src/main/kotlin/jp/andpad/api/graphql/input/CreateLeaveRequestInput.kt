package jp.andpad.api.graphql.input

data class CreateLeaveRequestInput(
    val startDate: String,
    val endDate: String,
    val reason: String,
)
