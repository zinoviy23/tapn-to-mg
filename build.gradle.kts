plugins {
    java
    kotlin("jvm") version "1.3.70"
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":tapaal"))

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileJava { setupJava11() }
    compileTestJava { setupJava11() }
}

fun JavaCompile.setupJava11() {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

val tapaalVersion = "3.6"

tasks.register<Exec>("getTapaal") {
    workingDir = projectDir
    commandLine("./tapaal_loader.sh", tapaalVersion)
}