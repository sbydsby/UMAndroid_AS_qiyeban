package com.sheca.zhongmei.model;

//import org.codehaus.jackson.annotate.JsonProperty;

public class GetOrgInfo {
	//@JsonProperty(value = "Result")
	private String Result;
	//@JsonProperty(value = "Return")
	private String Return;
	//@JsonProperty(value = "AccountUID")
	private String AccountUID;
	//@JsonProperty(value = "Name")
	private String Name;
	//@JsonProperty(value = "AgentEmail")
	private String AgentEmail;
	//@JsonProperty(value = "AgentMobile")
	private String AgentMobile;
	//@JsonProperty(value = "AgentName")
	private String AgentName;
	//@JsonProperty(value = "AgentIdentityCode")
	private String AgentIdentityCode;
	//@JsonProperty(value = "Type")
	private int Type;
	//@JsonProperty(value = "Status")
	private int Status;
	//@JsonProperty(value = "IsValid")
	private int IsValid;
	//@JsonProperty(value = "Source")
	private int Source;
	//@JsonProperty(value = "RegisterTime")
	private String RegisterTime;
	//@JsonProperty(value = "NotifyMode")
	private int NotifyMode;
	//@JsonProperty(value = "CountryName")
	private String CountryName;
	//@JsonProperty(value = "ProvinceName")
	private String ProvinceName;
	//@JsonProperty(value = "LocalityName")
	private String LocalityName;
	//@JsonProperty(value = "OrgUnitName")
	private String OrgUnitName;
	//@JsonProperty(value = "CreditNumber")
	private String CreditNumber;
	//@JsonProperty(value = "OrganizationCode")
	private String OrganizationCode;
	//@JsonProperty(value = "ICRegistrationNumber")
	private String ICRegistrationNumber;
	//@JsonProperty(value = "TaxationNumber")
	private String TaxationNumber;
	//@JsonProperty(value = "AccumulationFundNumber")
	private String AccumulationFundNumber;
	//@JsonProperty(value = "InstitutionsNumber")
	private String InstitutionsNumber;
	//@JsonProperty(value = "AssociationNumber")
	private String AssociationNumber;
	//@JsonProperty(value = "GovernmentInstitutionsNumber")
	private String GovernmentInstitutionsNumber;
	//@JsonProperty(value = "LawFirmLicence")
	private String LawFirmLicence;
	//@JsonProperty(value = "IndividualICLicence")
	private String IndividualICLicence;
	//@JsonProperty(value = "ForeignCompanyRegisterCode")
	private String ForeignCompanyRegisterCode;
	//@JsonProperty(value = "InsuranceNumber")
	private String InsuranceNumber;
	//@JsonProperty(value = "IdentityType")
	private int IdentityType;
	//@JsonProperty(value = "IdentityCode")
	private String IdentityCode;
		
	public String getAgentEmail() {
		return AgentEmail;
	}
	public void setAgentEmail(String agentEmail) {
		AgentEmail = agentEmail;
	}
	public String getAgentMobile() {
		return AgentMobile;
	}
	public void setAgentMobile(String agentMobile) {
		AgentMobile = agentMobile;
	}
	public String getAgentName() {
		return AgentName;
	}
	public void setAgentName(String agentName) {
		AgentName = agentName;
	}
	public String getAgentIdentityCode() {
		return AgentIdentityCode;
	}
	public void setAgentIdentityCode(String agentIdentityCode) {
		AgentIdentityCode = agentIdentityCode;
	}
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
	public int getStatus() {
		return Status;
	}
	public void setStatus(int status) {
		Status = status;
	}
	public int getIsValid() {
		return IsValid;
	}
	public void setIsValid(int isValid) {
		IsValid = isValid;
	}
	public int getSource() {
		return Source;
	}
	public void setSource(int source) {
		Source = source;
	}
	public String getRegisterTime() {
		return RegisterTime;
	}
	public void setRegisterTime(String registerTime) {
		RegisterTime = registerTime;
	}
	public int getNotifyMode() {
		return NotifyMode;
	}
	public void setNotifyMode(int notifyMode) {
		NotifyMode = notifyMode;
	}
	public String getCountryName() {
		return CountryName;
	}
	public void setCountryName(String countryName) {
		CountryName = countryName;
	}
	public String getProvinceName() {
		return ProvinceName;
	}
	public void setProvinceName(String provinceName) {
		ProvinceName = provinceName;
	}
	public String getLocalityName() {
		return LocalityName;
	}
	public void setLocalityName(String localityName) {
		LocalityName = localityName;
	}
	public String getOrgUnitName() {
		return OrgUnitName;
	}
	public void setOrgUnitName(String orgUnitName) {
		OrgUnitName = orgUnitName;
	}
	public String getCreditNumber() {
		return CreditNumber;
	}
	public void setCreditNumber(String creditNumber) {
		CreditNumber = creditNumber;
	}
	public String getOrganizationCode() {
		return OrganizationCode;
	}
	public void setOrganizationCode(String organizationCode) {
		OrganizationCode = organizationCode;
	}
	public String getICRegistrationNumber() {
		return ICRegistrationNumber;
	}
	public void setICRegistrationNumber(String iCRegistrationNumber) {
		ICRegistrationNumber = iCRegistrationNumber;
	}
	public String getTaxationNumber() {
		return TaxationNumber;
	}
	public void setTaxationNumber(String taxationNumber) {
		TaxationNumber = taxationNumber;
	}
	public String getAccumulationFundNumber() {
		return AccumulationFundNumber;
	}
	public void setAccumulationFundNumber(String accumulationFundNumber) {
		AccumulationFundNumber = accumulationFundNumber;
	}
	public String getInstitutionsNumber() {
		return InstitutionsNumber;
	}
	public void setInstitutionsNumber(String institutionsNumber) {
		InstitutionsNumber = institutionsNumber;
	}
	public String getAssociationNumber() {
		return AssociationNumber;
	}
	public void setAssociationNumber(String associationNumber) {
		AssociationNumber = associationNumber;
	}
	public String getGovernmentInstitutionsNumber() {
		return GovernmentInstitutionsNumber;
	}
	public void setGovernmentInstitutionsNumber(String governmentInstitutionsNumber) {
		GovernmentInstitutionsNumber = governmentInstitutionsNumber;
	}
	public String getLawFirmLicence() {
		return LawFirmLicence;
	}
	public void setLawFirmLicence(String lawFirmLicence) {
		LawFirmLicence = lawFirmLicence;
	}
	public String getIndividualICLicence() {
		return IndividualICLicence;
	}
	public void setIndividualICLicence(String individualICLicence) {
		IndividualICLicence = individualICLicence;
	}
	public String getForeignCompanyRegisterCode() {
		return ForeignCompanyRegisterCode;
	}
	public void setForeignCompanyRegisterCode(String foreignCompanyRegisterCode) {
		ForeignCompanyRegisterCode = foreignCompanyRegisterCode;
	}
	public String getInsuranceNumber() {
		return InsuranceNumber;
	}
	public void setInsuranceNumber(String insuranceNumber) {
		InsuranceNumber = insuranceNumber;
	}
	public int getIdentityType() {
		return IdentityType;
	}
	public void setIdentityType(int identityType) {
		IdentityType = identityType;
	}
	public String getIdentityCode() {
		return IdentityCode;
	}
	public void setIdentityCode(String identityCode) {
		IdentityCode = identityCode;
	}
	
	public String getResult() {
		return Result;
	}
	public void setResult(String result) {
		Result = result;
	}
	public String getReturn() {
		return Return;
	}
	public void setReturn(String return1) {
		Return = return1;
	}
	public String getAccountUID() {
		return AccountUID;
	}
	public void setAccountUID(String accountUID) {
		AccountUID = accountUID;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
}
