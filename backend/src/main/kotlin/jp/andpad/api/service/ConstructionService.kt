package jp.andpad.api.service

import jp.andpad.api.domain.ConstructionProject
import jp.andpad.api.domain.ProjectModuleRecord
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.graphql.input.CreateConstructionProjectInput
import jp.andpad.api.graphql.input.CreateProjectModuleRecordInput
import jp.andpad.api.repository.ConstructionRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

/**
 * 工事プロジェクト・モジュールレコード管理サービス。
 *
 * **責務**: プロジェクト CRUD、SaaS モジュール別レコード管理。
 * orgId は [TenantContext] から注入。
 *
 * **テナント分離**: 全操作で TenantContext.orgId() を使用。
 */
@Service
class ConstructionService(
    private val constructionRepository: ConstructionRepository,
) {

    /** 組織内工事プロジェクト一覧。 */
    fun listProjects(): List<ConstructionProject> {
        return constructionRepository.listProjects(TenantContext.orgId())
    }

    /** 新規工事プロジェクト作成。 */
    fun createProject(input: CreateConstructionProjectInput): ConstructionProject {
        return constructionRepository.createProject(
            TenantContext.orgId(),
            input.name,
            input.siteAddress,
            input.status,
            input.managerName,
            Dates.parseDate(input.startDate),
            Dates.parseDate(input.endDate),
        )
    }

    /** モジュールレコード一覧（moduleCode 必須）。 */
    fun listModuleRecords(moduleCode: SaasModuleCode?, projectId: String?): List<ProjectModuleRecord> {
        return constructionRepository.listModuleRecords(TenantContext.orgId(), moduleCode, projectId)
    }

    /** モジュールレコード作成。 */
    fun createModuleRecord(input: CreateProjectModuleRecordInput): ProjectModuleRecord {
        return constructionRepository.createModuleRecord(
            TenantContext.orgId(),
            input.projectId,
            input.moduleCode,
            input.title,
            input.status,
            input.detail,
            input.amount,
            input.personName,
            Dates.parseDate(input.recordDate),
        )
    }
}
