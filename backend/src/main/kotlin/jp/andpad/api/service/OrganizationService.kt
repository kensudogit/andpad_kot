package jp.andpad.api.service

import jp.andpad.api.domain.Organization
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.TeamMember
import jp.andpad.api.domain.UsageSummary
import jp.andpad.api.graphql.input.UpdateOrganizationInput
import jp.andpad.api.repository.OrganizationRepository
import jp.andpad.api.repository.SaasRepository
import jp.andpad.api.security.TenantContext
import org.springframework.stereotype.Service

/**
 * 組織・チーム管理サービス。
 *
 * **責務**: 組織情報取得／更新、チームメンバー一覧、利用状況サマリ。
 * 有効モジュール一覧は [SaasRepository] から取得して Organization に付与。
 *
 * **テナント分離**: TenantContext.orgId() を全操作で使用。
 */
@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val saasRepository: SaasRepository,
) {

    /** 現在テナントの組織情報（有効モジュール付き）。 */
    fun getOrganization(): Organization {
        val orgId = TenantContext.orgId()
        val modules = saasRepository.listOrgModules(orgId)
        return organizationRepository.getOrganization(orgId, modules)
    }

    /** 既存 Organization に最新モジュール一覧を付与して返す（認証フロー用）。 */
    fun enrichOrganization(organization: Organization): Organization {
        val modules = saasRepository.listOrgModules(organization.id)
        return Organization(
            organization.id,
            organization.name,
            organization.slug,
            organization.planTier,
            organization.subscriptionStatus,
            organization.seatCount,
            organization.timezone,
            organization.memberCount,
            organization.createdAt,
            modules,
        )
    }

    /** 組織の利用状況サマリ（メンバー数・プロジェクト数・API/相談利用量）。 */
    fun usageSummary(): UsageSummary {
        return organizationRepository.usageSummary(TenantContext.orgId())
    }

    /** チームメンバー一覧。 */
    fun teamMembers(): List<TeamMember> {
        return organizationRepository.listTeamMembers(TenantContext.orgId())
    }

    /**
     * 組織属性を部分更新（認証必須）。
     *
     * null のフィールドは更新対象外。
     */
    fun updateOrganization(input: UpdateOrganizationInput): Organization {
        TenantContext.requirePrincipal()
        val patch = hashMapOf<String, Any>()
        if (input.name != null) {
            patch["name"] = input.name
        }
        if (input.slug != null) {
            patch["slug"] = input.slug
        }
        if (input.seatCount != null) {
            patch["seatCount"] = input.seatCount
        }
        if (input.timezone != null) {
            patch["timezone"] = input.timezone
        }
        val orgId = TenantContext.orgId()
        val modules = saasRepository.listOrgModules(orgId)
        return organizationRepository.updateOrganization(orgId, patch, modules)
    }
}
