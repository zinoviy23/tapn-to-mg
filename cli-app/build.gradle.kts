plugins {
    java
    application
}

group = "com.github.zinoviy23"
version = "0.0"

tasks.distZip {
    archiveBaseName.set("tapn-to-mg")
}

tasks.startScripts {
    applicationName = "tapn-to-mg"
}

tasks.installDist {
    destinationDir = file("$buildDir/install/tapn-to-mg")
}

repositories {
    mavenCentral()
}

application {
    mainClassName = "com.github.zinoviy23.TapnToMgApp"
}

dependencies {
    implementation("info.picocli:picocli:4.2.0")
    annotationProcessor("info.picocli:picocli-codegen:4.2.0")

    implementation(project(":converters"))
    implementation(project(":tapaal.ext"))

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks {
    compileJava {
        options.compilerArgs = options.compilerArgs + listOf("-Aproject=${project.group}/${project.name}")
    }
}