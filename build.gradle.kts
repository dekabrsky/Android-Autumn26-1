plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.javafaker:javafaker:1.0.2")
    // Задание 4: диаграммы состава
    implementation("org.jfree:jfreechart:1.5.4")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8",
        "-Djava.awt.headless=true",
    )
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
