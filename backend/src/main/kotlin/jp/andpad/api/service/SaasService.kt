package jp.andpad.api.service

import jp.andpad.api.domain.AttendanceRecord
import jp.andpad.api.domain.Contract
import jp.andpad.api.domain.ContractTemplate
import jp.andpad.api.domain.CrmContact
import jp.andpad.api.domain.CrmInteraction
import jp.andpad.api.domain.DxInitiative
import jp.andpad.api.domain.LeaveRequest
import jp.andpad.api.domain.SaasModule
import jp.andpad.api.domain.SaasModuleCode
import jp.andpad.api.graphql.input.CreateContractInput
import jp.andpad.api.graphql.input.CreateCrmContactInput
import jp.andpad.api.graphql.input.CreateDxInitiativeInput
import jp.andpad.api.graphql.input.CreateLeaveRequestInput
import jp.andpad.api.repository.SaasRepository
import jp.andpad.api.security.TenantContext
import jp.andpad.api.util.Dates
import org.springframework.stereotype.Service

@Service
class SaasService(
    private val saasRepository: SaasRepository,
) {

    fun saasModules(): List<SaasModule> {
        return saasRepository.listOrgModules(TenantContext.orgId())
    }

    fun setModuleEnabled(code: SaasModuleCode, enabled: Boolean): SaasModule {
        return saasRepository.setModuleEnabled(TenantContext.orgId(), code, enabled)
    }

    fun dxInitiatives(): List<DxInitiative> {
        return saasRepository.listDxInitiatives(TenantContext.orgId())
    }

    fun createDxInitiative(input: CreateDxInitiativeInput): DxInitiative {
        return saasRepository.createDxInitiative(
            TenantContext.orgId(),
            input.title,
            input.description,
            input.status,
            input.progressPct ?: 0,
            input.ownerName,
            Dates.parseDate(input.dueDate),
        )
    }

    fun crmContacts(): List<CrmContact> {
        return saasRepository.listCrmContacts(TenantContext.orgId())
    }

    fun createCrmContact(input: CreateCrmContactInput): CrmContact {
        return saasRepository.createCrmContact(
            TenantContext.orgId(),
            input.name,
            input.email,
            input.phone,
            input.company,
            input.stage,
            input.notes,
        )
    }

    fun crmInteractions(contactId: String): List<CrmInteraction> {
        return saasRepository.listCrmInteractions(TenantContext.orgId(), contactId)
    }

    fun createCrmInteraction(contactId: String, kind: String, summary: String): CrmInteraction {
        return saasRepository.createCrmInteraction(TenantContext.orgId(), contactId, kind, summary)
    }

    fun attendanceRecords(): List<AttendanceRecord> {
        return saasRepository.listAttendanceRecords(TenantContext.orgId())
    }

    fun clockIn(note: String?): AttendanceRecord {
        return saasRepository.clockIn(TenantContext.orgId(), TenantContext.userId(), note ?: "")
    }

    fun clockOut(): AttendanceRecord {
        return saasRepository.clockOut(TenantContext.orgId(), TenantContext.userId())
    }

    fun leaveRequests(): List<LeaveRequest> {
        return saasRepository.listLeaveRequests(TenantContext.orgId())
    }

    fun createLeaveRequest(input: CreateLeaveRequestInput): LeaveRequest {
        val start = Dates.parseDate(input.startDate)
            ?: throw IllegalArgumentException("invalid startDate")
        val end = Dates.parseDate(input.endDate)
            ?: throw IllegalArgumentException("invalid endDate")
        return saasRepository.createLeaveRequest(
            TenantContext.orgId(),
            TenantContext.userId(),
            start,
            end,
            input.reason,
        )
    }

    fun approveLeaveRequest(id: String): LeaveRequest {
        return saasRepository.approveLeaveRequest(TenantContext.orgId(), id)
    }

    fun contractTemplates(): List<ContractTemplate> {
        return saasRepository.listContractTemplates(TenantContext.orgId())
    }

    fun createContractTemplate(name: String, body: String): ContractTemplate {
        return saasRepository.createContractTemplate(TenantContext.orgId(), name, body)
    }

    fun contracts(): List<Contract> {
        return saasRepository.listContracts(TenantContext.orgId())
    }

    fun createContract(input: CreateContractInput): Contract {
        return saasRepository.createContract(
            TenantContext.orgId(),
            input.templateId,
            input.title,
            input.partyName,
            input.partyEmail,
            input.body,
        )
    }

    fun signContract(id: String): Contract {
        return saasRepository.signContract(TenantContext.orgId(), id)
    }
}
