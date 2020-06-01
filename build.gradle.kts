buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradleBuildTool}")
        classpath(kotlin("gradle-plugin", version = Versions.kotlin))
        classpath("com.novoda:bintray-release:${Versions.bintrayRelease}")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:${Versions.spotless}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

subprojects {
    apply(plugin = "com.diffplug.gradle.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
            licenseHeaderFile("../spotless.license.kt")
        }
    }
}