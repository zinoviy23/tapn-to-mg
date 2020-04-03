plugins {
    java
    jacoco
    `java-library`
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(group = "org.jgrapht", name = "jgrapht-core", version = "1.4.0")

    implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = "2.10.3")
    implementation(group = "org.everit.json", name = "org.everit.json.schema", version = "1.5.1")

    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.13.1")
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.13.1")

    compileOnly("org.jetbrains:annotations:19.0.0")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

jacoco {
    toolVersion = "0.8.5"
    reportsDir = file("$buildDir/customJacocoReportDir")
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacocoHtml")
    }
}