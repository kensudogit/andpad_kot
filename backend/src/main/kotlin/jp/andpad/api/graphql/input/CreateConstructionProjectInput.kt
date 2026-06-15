package jp.andpad.api.graphql.input

import jp.andpad.api.domain.ConstructionProjectStatus

data class CreateConstructionProjectInput(
    val name: String,
    val siteAddress: String,
    val status: ConstructionProjectStatus,
    val managerName: String,
    val startDate: String,
    val endDate: String,
)
