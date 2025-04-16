package Diadoc.Api

import Diadoc.Api.sign.GOSTSignInfoProvider
import com.objsys.asn1j.runtime.*
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name
import ru.CryptoPro.JCP.KeyStore.Rutoken.RutokenStore
import ru.CryptoPro.JCP.KeyStore.Rutoken.stores.*
import ru.CryptoPro.JCP.Util.JCPInit
import ru.CryptoPro.JCP.params.OID
import ru.CryptoPro.JCSP.JCSP
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.cert.*

object CertificateHelper {

    private const val STR_CMS_OID_DATA = "1.2.840.113549.1.7.1"
    private const val STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2"

    @Throws(Exception::class)
    fun createCMS(
        buffer: ByteArray?,
        sign: ByteArray?,
        cert: Certificate,
        detached: Boolean,
        gostSignInfoProvider: GOSTSignInfoProvider
    ): ByteArray {
        val all = ContentInfo()
        all.contentType = Asn1ObjectIdentifier(OID(STR_CMS_OID_SIGNED).value)
        val cms = SignedData()
        all.content = cms
        cms.version = CMSVersion(1)
        // digest
        cms.digestAlgorithms = DigestAlgorithmIdentifiers(1)
        val a = DigestAlgorithmIdentifier(
            OID(gostSignInfoProvider.digestOID).value
        )
        a.parameters = Asn1Null()
        cms.digestAlgorithms.elements[0] = a
        if (detached) {
            cms.encapContentInfo = EncapsulatedContentInfo(
                Asn1ObjectIdentifier(
                    OID(STR_CMS_OID_DATA).value
                ), null
            )
        } else {
            cms.encapContentInfo = EncapsulatedContentInfo(
                Asn1ObjectIdentifier(
                    OID(STR_CMS_OID_DATA).value
                ),
                Asn1OctetString(buffer)
            )
        }
        // certificate
        cms.certificates = CertificateSet(1)
        val certificate = ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate()
        val decodeBuffer = Asn1BerDecodeBuffer(
            cert.encoded
        )
        certificate.decode(decodeBuffer)
        cms.certificates.elements = arrayOfNulls(1)
        cms.certificates.elements[0] = CertificateChoices()
        cms.certificates.elements[0].set_certificate(certificate)

        // signer info
        cms.signerInfos = SignerInfos(1)
        cms.signerInfos.elements[0] = SignerInfo()
        cms.signerInfos.elements[0].version = CMSVersion(1)
        cms.signerInfos.elements[0].sid = SignerIdentifier()

        val encodedName = (cert as X509Certificate)
            .issuerX500Principal.encoded
        val nameBuf = Asn1BerDecodeBuffer(encodedName)
        val name = Name()
        name.decode(nameBuf)

        val num = CertificateSerialNumber(
            cert.serialNumber
        )
        cms.signerInfos.elements[0].sid
            .set_issuerAndSerialNumber(IssuerAndSerialNumber(name, num))
        cms.signerInfos.elements[0].digestAlgorithm = DigestAlgorithmIdentifier(
            OID(gostSignInfoProvider.digestOID).value
        )
        cms.signerInfos.elements[0].digestAlgorithm.parameters = Asn1Null()
        cms.signerInfos.elements[0].signatureAlgorithm = SignatureAlgorithmIdentifier(
            OID(gostSignInfoProvider.signOID).value
        )
        cms.signerInfos.elements[0].signatureAlgorithm.parameters = Asn1Null()
        cms.signerInfos.elements[0].signature = SignatureValue(sign)
        // encode
        val asnBuf = Asn1BerEncodeBuffer()
        all.encode(asnBuf, true)
        return asnBuf.msgCopy
    }

    @Throws(Exception::class)
    fun CMSSign(
        data: ByteArray?, key: PrivateKey?, cert: Certificate,
        detached: Boolean
    ): ByteArray {
        // sign

        val signatureInfoProvider = GOSTSignInfoProvider(cert as X509Certificate)
        val signature = signatureInfoProvider.signatureInstance

        signature.initSign(key)
        signature.update(data)
        val sign = signature.sign()
        // create cms format
        return createCMS(data, sign, cert, detached, signatureInfoProvider)
    }

    @Throws(Exception::class)
    fun sign(cert: X509Certificate, data: ByteArray?): ByteArray {
        val privateKey = getPrivateKey(cert, null)
        return CMSSign(data, privateKey, cert, true)
    }

    fun getPrivateKey(cert: X509Certificate, password: CharArray?): PrivateKey? {
        try {
            val keystore = KeyStore.getInstance(JCSP.MY_STORE_NAME, JCSP.PROVIDER_NAME)
            keystore.load(null, null)
            val en = keystore.aliases()
            while (en.hasMoreElements()) {
                val s = en.nextElement()
                if (keystore.isKeyEntry(s)) {
                    val kcerts = keystore.getCertificateChain(s)
                    if (kcerts[0] is X509Certificate) {
                        val x509 = kcerts[0] as X509Certificate
                        if (getThumbPrint(x509).startsWith(getThumbPrint(cert))) return keystore.getKey(
                            s,
                            password
                        ) as PrivateKey
                    }
                }
                if (keystore.isCertificateEntry(s)) {
                    val c = keystore.getCertificate(s)
                    if (c is X509Certificate) {
                        if (getThumbPrint(c).startsWith(getThumbPrint(cert))) return keystore.getKey(
                            s,
                            password
                        ) as PrivateKey
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getPrivateKeyFromAllCerts(cert: X509Certificate, keyStore: KeyStore?, password: CharArray?): PrivateKey? {
        if(keyStore != null) {
            val alias = keyStore.getCertificateAlias(cert)

            val key = try {
                keyStore.getKey(alias, null)
            } catch (e: Exception) {
                return null
            }

            return key as PrivateKey
        }

        for (i in getCertificatesAll()) {
            if (cert == i.cert) {
                val alias = i.keyStore.getCertificateAlias(cert)

                val key = try {
                    i.keyStore.getKey(alias, null)
                } catch (e: Exception) {
                    continue
                }

                return key as PrivateKey
            }
        }
        return null
    }

    fun CertificateToString(certificate: X509Certificate): String {
        val subject = certificate.subjectDN.name
        val cnStartIndex = subject.indexOf("CN=")
        if (cnStartIndex == -1) return subject
        val commonName = subject.substring(cnStartIndex + 3)
        var commaIndex = commonName.indexOf(',')
        if (commaIndex == -1) commaIndex = commonName.indexOf(';')
        if (commaIndex == -1) return commonName
        return commonName.substring(0, commaIndex)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(NoSuchAlgorithmException::class, CertificateEncodingException::class)
    fun getThumbPrint(cert: X509Certificate): String {
        val md = MessageDigest.getInstance("SHA-1")
        val der = cert.encoded
        md.update(der)
        val digest = md.digest()
        return digest.toHexString(HexFormat.Default)
    }

    fun getCertificatesFromPersonalStore(): List<X509Certificate> {
            val certs: MutableList<X509Certificate> = ArrayList()
            try {
                JCPInit.initProviders(true)
                val keystore = KeyStore.getInstance(JCSP.MY_STORE_NAME, JCSP.PROVIDER_NAME)
                keystore.load(null, null)
                val en = keystore.aliases()
                while (en.hasMoreElements()) {
                    val s = en.nextElement()
                    if (keystore.isKeyEntry(s)) {
                        val kcerts = keystore.getCertificateChain(s)
                        if (kcerts[0] is X509Certificate) {
                            val x509 = kcerts[0] as X509Certificate
                            certs.add(x509)
                        }
                    }
                    if (keystore.isCertificateEntry(s)) {
                        val c = keystore.getCertificate(s)
                        if (c is X509Certificate) {
                            certs.add(c)
                        }
                    }
                }
                return certs
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return certs
        }

    fun getCertificatesAll(rutoken: Boolean = false, myStore: Boolean = false): List<CertItem> {
            var localRutoken = false
            var localMyStore = false

        if (!rutoken && !myStore) {
                localRutoken = true
                localMyStore = true
            } else {
                localRutoken = rutoken
                localMyStore = myStore
            }

        JCPInit.initProvidersFromCheckConfFull(true)
        val certs: MutableList<CertItem> = mutableListOf()

        if (localRutoken) {
            val listRutokens = JCSP().services.filter {
                it.algorithm.uppercase().contains("RUTOKEN")
            }.toList()

            for (service in listRutokens) {
                val rutokenStoreC = service.algorithm
                val keyStore = try {
                    KeyStore.getInstance(rutokenStoreC, JCSP.PROVIDER_NAME)

                } catch (e: Exception) {
                    continue
                }
                keyStore.load(null, null)
                val aliases = try {
                    keyStore.aliases()
                } catch (e: Exception) {
                    continue
                }
                for (alias in aliases) {
                    try {
                        certs.add(CertItem(keyStore, keyStore.getCertificate(alias) as X509Certificate))
                    } catch (e: Exception) {

                    }
                }
            }
        }

        if (localMyStore) {
            val certsMy = getCertificatesFromPersonalStore()
            val keyStoreMy = KeyStore.getInstance(JCSP.MY_STORE_NAME, JCSP.PROVIDER_NAME)
            keyStoreMy.load(null, null)
            for (certMy in certsMy) {
                certs.add(CertItem(keyStoreMy, certMy))
            }
        }

            return certs
        }

    @Throws(CertificateException::class)
    fun getCertificateFromBytes(bytes: ByteArray?): X509Certificate {
        val certFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(bytes)) as X509Certificate
    }
}