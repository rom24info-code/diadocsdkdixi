plugins {
    kotlin("jvm") version "2.0.0"
}

group = "ru.dixi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    flatDir {
        dirs("libs/java-csp-5.0.42119-A")
    }
}

dependencies {
    implementation(":JCSP")
    implementation(":ASN1P")
    implementation(":asn1rt")
    implementation(":CAdES")
    implementation(":XAdES")
    implementation(":JCP")
    implementation(":cmsutil")
    implementation(":cpSSL")
    implementation(":JCPRevCheck")
    implementation(":JCPRevTools")
    implementation(":JCryptoP")
    implementation(":AdES-core")
    implementation(":Rutoken")

    implementation("org.cryptacular:cryptacular:1.2.6")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15on
    implementation("org.bouncycastle:bcpkix-jdk15on:1.60")
    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on
    implementation("org.bouncycastle:bcprov-jdk15on:1.60")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-okio-jvm:1.7.1")

    // https://mvnrepository.com/artifact/org.springframework/spring-web
    implementation("org.springframework:spring-web:6.1.10")

    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    // https://mvnrepository.com/artifact/javax.mail/mail
    implementation("javax.mail:mail:1.4.7")
    // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
    implementation("com.google.protobuf:protobuf-java:3.23.3")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.13.0")

    //XML
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.17.1")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    exclude("MainKt.class")
    exclude("TestTicksKt.class")
}