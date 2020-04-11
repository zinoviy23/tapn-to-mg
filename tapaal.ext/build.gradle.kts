plugins {
    java
    application
}

group = "com.github.zinoviy23"
version = "0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":tapaal"))

    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}

configure<JavaApplication> {
    mainClassName = "com.github.zinoviy23.tapaal.extended.ExtendedTapaal"
}