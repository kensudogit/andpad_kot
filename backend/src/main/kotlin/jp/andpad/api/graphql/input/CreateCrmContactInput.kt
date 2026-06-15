package jp.andpad.api.graphql.input

data class CreateCrmContactInput(
    val name: String,
    val email: String,
    val phone: String,
    val company: String,
    val stage: String,
    val notes: String,
)
