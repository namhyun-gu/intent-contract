import com.novoda.gradle.release.PublishExtension

plugins {
    `java-library`
    kotlin("jvm")
}

apply<com.novoda.gradle.release.ReleasePlugin>()

dependencies {
    implementation(Deps.Kotlin.stdlib)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

configure<PublishExtension> {
    userOrg = "namhyun-gu"
    groupId = "dev.namhyun.intentcontract"
    artifactId = "intentcontract-annotations"
    publishVersion = Versions.version
    desc = ""
    website = "https://github.com/namhyun-gu/intent-contract"
    repoName = "intentcontract"
}