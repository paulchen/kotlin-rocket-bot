import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.6.1"
val coroutinesVersion = "1.3.8"
var commonsCodecVersion = "1.15"
val gsonVersion = "2.8.7"


plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.palantir.docker") version "0.26.0"
}

group = "at.rueckgr.kotlin.rocketbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("commons-codec:commons-codec:${commonsCodecVersion}")
    implementation("com.google.code.gson:gson:$gsonVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "at.rueckgr.kotlin.rocketbot.MainKt"
}

distributions {
    main {
        version = ""
    }
}

docker {
    name = "${project.name}:latest"
    files("build/distributions")
}
