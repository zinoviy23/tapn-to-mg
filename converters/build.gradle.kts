plugins {
    java
    `java-library`
    jacoco
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":metric-graphs"))
    api(project(":tapaal"))

    compileOnly("org.jetbrains:annotations:19.0.0")

    testImplementation("junit", "junit", "4.12")
    testImplementation("org.assertj:assertj-core:3.15.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    systemProperty("java.awt.headless", "true")
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