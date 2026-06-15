package jp.andpad.api.graphql.input

data class UpdateOrganizationInput(
    val name: String,
    val slug: String,
    val seatCount: Int?,
    val timezone: String,
)
