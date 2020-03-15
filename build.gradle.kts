group = "com.github.zinoviy23"
version = "0.0"

val tapaalVersion = "3.6"

tasks.register<Exec>("getTapaal") {
    workingDir = projectDir
    commandLine("./tapaal_loader.sh", tapaalVersion)
}