package Diadoc.Api

import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate

class CertItem(val keyStore: KeyStore, val cert: X509Certificate)