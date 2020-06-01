import com.novoda.gradle.release.PublishExtension

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
}

apply<com.novoda.gradle.release.ReleasePlugin>()

dependencies {
    implementation(Deps.Kotlin.stdlib)
    implementation(project(":intentcontract-annotations"))
    implementation(Deps.kotlinpoet)
    implementation(Deps.incap)
    kapt(Deps.incapProcessor)
    implementation(Deps.autoService)
    kapt(Deps.autoService)
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

configure<PublishExtension> {
    userOrg = "namhyun-gu"
    groupId = "dev.namhyun.intentcontract"
    artifactId = "intentcontract-compiler"
    publishVersion = Versions.version
    desc = ""
    website = "https://github.com/namhyun-gu/intent-contract"
    repoName = "intentcontract"
}