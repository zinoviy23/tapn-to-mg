plugins {
    java
    kotlin("jvm") version "1.3.70"
    application
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

application {
    mainClassName = "com.github.zinoviy23.TapnToMgApp"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("info.picocli:picocli:4.2.0")
    annotationProcessor("info.picocli:picocli-codegen:4.2.0")

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileJava {
        options.compilerArgs = options.compilerArgs + listOf("-Aproject=${project.group}/${project.name}")
    }
}