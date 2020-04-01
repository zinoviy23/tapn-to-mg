plugins {
    java
    jacoco
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "org.jgrapht", name = "jgrapht-core", version = "1.4.0")
    implementation(group = "org.jgrapht", name = "jgrapht-io", version = "1.4.0")

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