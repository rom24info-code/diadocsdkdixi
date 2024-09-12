package Diadoc.Api.document

import Diadoc.Api.DiadocApi
import Diadoc.Api.Proto.Documents.DocumentProtos.Document
import Diadoc.Api.XML.UniversalTransferDocumentBuyerTitle_utd820_05_01_02_hyphen
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

fun DiadocApi.acceptDocument(document: Document,
                             boxId: String) {
    val xml = getXmlTitle()
    generateClient.generateTitleXml(
        boxId,
        document.typeNamedId,
        document.function,
        document.version,
        1,
        xml,
        null,
        false,
        document.messageId,
        document.entityId)
}

private fun getXmlTitle(): ByteArray {
    val xmlMapper = XmlMapper()
    xmlMapper.enable(SerializationFeature.INDENT_OUTPUT)
    xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
    val utdUser = UniversalTransferDocumentBuyerTitle_utd820_05_01_02_hyphen()
    utdUser.documentCreator = "ООО Чудодей Владивосток"
    utdUser.operationContent = "Товары принял без претензий"
    utdUser.employee.lastName = "Бобков"
    utdUser.employee.firstName = "Алексей"
    utdUser.employee.middleName = "Александрович"
    utdUser.employee.position = "Генеральный директор"
    utdUser.employee.employeeBase = "Генеральный директор"
    utdUser.signers.signerDetails.inn = "2536189827"
    utdUser.signers.signerDetails.lastName = "Бобков"
    utdUser.signers.signerDetails.firstName = "Алексей"
    utdUser.signers.signerDetails.middleName = "Александрович"
    utdUser.signers.signerDetails.signerType = "1"
    utdUser.signers.signerDetails.signerPowers = "1"
    utdUser.signers.signerDetails.signerStatus = "5"
    utdUser.signers.signerDetails.position = "Генеральный директор"
    val universalTransferDocumentBuyerTitle = xmlMapper.writeValueAsString(utdUser)

    return universalTransferDocumentBuyerTitle.toByteArray()
}

fun main() {
}