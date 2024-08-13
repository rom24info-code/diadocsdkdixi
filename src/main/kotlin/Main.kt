package ru.dixi

import Diadoc.Api.*
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos.EntityType
import Diadoc.Api.document.DocumentsFilter
import ru.CryptoPro.CAdES.EnvelopedSignature
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.cert.X509Certificate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val CHUDODEY_THUMBPRINT = "3dd9d9342b22b9c3f35e0cddfceb2c5b8286eef2"
private const val addressApiDiadoc = "https://diadoc-api.kontur.ru"
private const val devKey = "1S82-5-48-4ebb53a8-cea0-4f25-a79c-ea90d40bf343"

fun main() {
    val diadocApi = DiadocApi(devKey, addressApiDiadoc)
    val certs = CertificateHelper.getCertificatesFromPersonalStore()

    for (cert in certs) {
        val thumbPrint = CertificateHelper.getThumbPrint(cert)
        println(thumbPrint)
        if (thumbPrint.uppercase() == CHUDODEY_THUMBPRINT.uppercase()) {
            diadocApi.authClient.authenticate(cert)

            val organizations = diadocApi.organizationClient.myOrganizations
            println(organizations.organizationsList.first().fullName)
            val boxId = organizations.organizationsList.first().boxesList.first().boxId
            if (diadocApi.authManager.isAuthenticated) {
                val docFilter = DocumentsFilter()
                docFilter.boxId = boxId
                docFilter.documentNumber = "ЦБ-970"
                docFilter.filterCategory =  "UniversalTransferDocument.Inbound"
                var afterIndexKey = ""
                var countDocs = 0
                while (true) {
                    docFilter.afterIndexKey = afterIndexKey
                    val result = diadocApi.documentClient.getDocuments(docFilter)
                    afterIndexKey = result.documentsList.last().indexKey

                    countDocs += result.documentsCount
                    println(result.documentsList.first().documentNumber + " " + result.documentsList.first().documentDate + " " + countDocs)
                    try {
                        val curDoc = result.documentsList.last()
                        val message = diadocApi.messageClient.getMessage(boxId, curDoc.messageId)
                        var signEntityId = ""
                        for (i in message.entitiesList) {
                            if (i.hasEntityType() && i.entityType == EntityType.Signature) {
                                signEntityId = i.entityId
                                break
                            }
                        }
                        val signerInfo = diadocApi.documentClient.getSignatureInfo(boxId, curDoc.messageId, signEntityId)
                        println(signerInfo)
                    } catch (e: Exception) {}

                    if (!result.hasMoreResults)
                        break
                }
            }

        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun decryptToken(data: ByteArray, cert: X509Certificate): String {
    val key = CertificateHelper.getPrivateKey(cert as X509Certificate, null)

    val decryptedByteDataStream = ByteArrayOutputStream()
    val signature = EnvelopedSignature(ByteArrayInputStream(data))
    signature.decrypt(cert, key, decryptedByteDataStream)
    val token = Base64.encode(decryptedByteDataStream.toByteArray())

    return token
}

