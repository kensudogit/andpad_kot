package jp.andpad.api.graphql.input

import jp.andpad.api.domain.SaasModuleCode

data class CreateProjectModuleRecordInput(
    val projectId: String,
    val moduleCode: SaasModuleCode,
    val title: String,
    val status: String,
    val detail: String,
    val amount: Double?,
    val personName: String,
    val recordDate: String,
)
