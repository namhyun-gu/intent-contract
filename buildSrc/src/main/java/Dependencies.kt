import java.io.File
import java.util.concurrent.TimeUnit

object Versions {
    val version = "git describe --tags".runCommand().split("-")[0]

    // Plugins
    const val kotlin = "1.3.72"
    const val spotless = "4.0.1"
    const val bintrayRelease = "0.9.2"
    const val gradleBuildTool = "4.0.0"

    // Dependencies
    const val kotlinpoet = "1.6.0"
    const val incap = "0.2"
    const val autoService = "1.0-rc6"
}

object Deps {
    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    }

    const val kotlinpoet = "com.squareup:kotlinpoet:${Versions.kotlinpoet}"
    const val incap = "net.ltgt.gradle.incap:incap:${Versions.incap}"
    const val incapProcessor = "net.ltgt.gradle.incap:incap-processor:${Versions.incap}"
    const val autoService = "com.google.auto.service:auto-service:${Versions.autoService}"
}

fun String.runCommand(workingDir: File = File("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}