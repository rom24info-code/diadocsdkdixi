package ru.dixi


import Diadoc.Api.CertificateHelper
import Diadoc.Api.DiadocApi

import Diadoc.Api.Proto.Docflow.DocflowApiProtos
import Diadoc.Api.Proto.Docflow.DocflowApiV3Protos.DocflowEventV3
import Diadoc.Api.Proto.Events.DiadocMessage_GetApiProtos.EntityType
import Diadoc.Api.Proto.TimestampProtos
import Diadoc.Api.Proto.TotalCountTypeProtos
import Diadoc.Api.document.DocumentsFilter
import java.security.cert.X509Certificate

private const val addressApiDiadoc = "https://diadoc-api.kontur.ru"
private const val devKey = "1S82-5-48-4ebb53a8-cea0-4f25-a79c-ea90d40bf343"

fun main() {



    val diadocApi = DiadocApi(devKey, addressApiDiadoc)
    val certs = CertificateHelper.getCertificatesAll(rutoken = true)//CertificateHelper.getCertificatesFromPersonalStore()

    for (certItem in certs) {
        val cert = certItem.cert
        val thumbPrint = CertificateHelper.getThumbPrint(cert as X509Certificate)
        println(thumbPrint)
        if (thumbPrint.uppercase() == "3dd9d9342b22b9c3f35e0cddfceb2c5b8286eef2".uppercase()) {
            try {
                diadocApi.authClient.authenticate(cert, null, null)
            } catch (e: Exception) {
                continue
            }

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

                    if (result.documentsList.count() == 0)
                        break

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

                val getDocflowRequest = DocflowApiProtos.GetDocflowEventsRequest.newBuilder()
                val timeStamp = TimestampProtos.Timestamp.newBuilder().setTicks(638397504000000000).build()
                getDocflowRequest.filterBuilder.setFromTimestamp(timeStamp).build()
                getDocflowRequest.populateDocuments = true
//                getDocflowRequest.injectEntityContent = true

                val listEvents = mutableListOf<DocflowEventV3>()
                while (true) {
                    val result = diadocApi.docflowClient.getDocflowEvents(boxId, getDocflowRequest.build())
                    getDocflowRequest.afterIndexKey = result.eventsList.last().indexKey
                    listEvents.addAll(result.eventsList)
                    println(result.eventsList.last().document.documentInfo.messageType)

                    if (result.totalCountType == TotalCountTypeProtos.TotalCountType.Equal) {
                        break
                    }

                }
            }

        }
    }
}

