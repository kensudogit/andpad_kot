package jp.andpad.api.graphql.input

data class CreateContractInput(
    val templateId: String,
    val title: String,
    val partyName: String,
    val partyEmail: String,
    val body: String,
)
