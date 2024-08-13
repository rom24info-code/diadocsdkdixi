package ru.dixi.Diadoc.Api

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("UniversalTransferDocumentBuyerTitle")
class UniversalTransferDocumentBuyerTitle {
    //Attributes
    @JacksonXmlProperty(localName = "DocumentCreator", isAttribute = true)
    var documentCreator = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "DocumentCreatorBase", isAttribute = true)
    var documentCreatorBase = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "OperationCode", isAttribute = true)
    var operationCode = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "OperationContent", isAttribute = true)
    var operationContent = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "AcceptanceDate", isAttribute = true)
    var acceptanceDate = ""

    //Tags
    @JacksonXmlProperty(localName = "Employee")
    var employee = Employee()

    @JacksonXmlProperty(localName = "Signers")
    var signers = Signers()


}

class Employee {
    //Attributes
    @JacksonXmlProperty(localName = "Position", isAttribute = true)
    var position = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "EmployeeInfo", isAttribute = true)
    var employeeInfo = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "EmployeeBase", isAttribute = true)
    var employeeBase = ""

    @JacksonXmlProperty(localName = "LastName", isAttribute = true)
    var lastName = ""

    @JacksonXmlProperty(localName = "FirstName", isAttribute = true)
    var firstName = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "MiddleName", isAttribute = true)
    var middleName = ""
}

class Signers {
    @JacksonXmlProperty(localName = "SignerDetails")
    var signerDetails = ExtendedSignerDetails_BuyerTitle820()

}

class ExtendedSignerDetails_BuyerTitle820: ExtendedSignerDetailsBase() {
    @JacksonXmlProperty(localName = "SignerPowers", isAttribute = true)
    var signerPowers = ""

    @JacksonXmlProperty(localName = "SignerStatus", isAttribute = true)
    var signerStatus = ""
}

open class ExtendedSignerDetailsBase {
    @JacksonXmlProperty(localName = "LastName", isAttribute = true)
    var lastName = ""

    @JacksonXmlProperty(localName = "FirstName", isAttribute = true)
    var firstName = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "MiddleName", isAttribute = true)
    var middleName = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "Position", isAttribute = true)
    var position = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "Inn", isAttribute = true)
    var inn = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "RegistrationCertificate", isAttribute = true)
    var registrationCertificate = ""

    @JacksonXmlProperty(localName = "SignerType", isAttribute = true)
    var signerType = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "SignerOrganizationName", isAttribute = true)
    var signerOrganizationName = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "SignerInfo", isAttribute = true)
    var signerInfo = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "SignerPowersBase", isAttribute = true)
    var signerPowersBase = ""

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JacksonXmlProperty(localName = "SignerOrgPowersBase", isAttribute = true)
    var signerOrgPowersBase = ""
}