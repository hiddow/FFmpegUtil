import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("aws.sdk.kotlin:s3:0.17.12-beta")
    implementation("aws.sdk.kotlin:polly:0.17.12-beta")
    implementation("aws.sdk.kotlin:translate:0.17.12-beta")
    implementation("net.imagej:ij:1.51h")
    implementation("net.bramp.ffmpeg:ffmpeg:0.7.0")
    implementation ("com.alibaba:easyexcel:3.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("com.googlecode.mp4parser:isoparser:1.1.22")
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}