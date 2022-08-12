import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    application
}

group = "me.nick"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
// TODO: cut back on unneeded dependencies
dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0-RC")
    implementation("org.nield:kotlin-statistics:1.2.1")
    implementation("com.github.dpaukov:combinatoricslib3:3.3.3")
    implementation("com.marcinmoskala:DiscreteMathToolkit:1.0.3")
    implementation("com.github.shiguruikai:combinatoricskt:1.6.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}