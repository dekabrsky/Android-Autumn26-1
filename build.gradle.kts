plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.21")
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.8.3")
    implementation("org.slf4j:slf4j-nop:2.0.13")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.yaml" && requested.name == "snakeyaml") {
            useVersion("2.2")
            artifactSelection {
                selectArtifact(requested.name, "jar", null)
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}