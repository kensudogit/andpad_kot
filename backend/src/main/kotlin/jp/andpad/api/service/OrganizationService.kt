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

@Service
class OrganizationService(
    private val organizationRepository: OrganizationRepository,
    private val saasRepository: SaasRepository,
) {

    fun getOrganization(): Organization {
        val orgId = TenantContext.orgId()
        val modules = saasRepository.listOrgModules(orgId)
        return organizationRepository.getOrganization(orgId, modules)
    }

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

    fun usageSummary(): UsageSummary {
        return organizationRepository.usageSummary(TenantContext.orgId())
    }

    fun teamMembers(): List<TeamMember> {
        return organizationRepository.listTeamMembers(TenantContext.orgId())
    }

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
