import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.6.1"
val coroutinesVersion = "1.3.8"
var commonsCodecVersion = "1.15"
val gsonVersion = "2.8.7"
val reflectionsVersion = "0.9.12"
val prettytimeVersion = "5.0.1.Final"

plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.palantir.docker") version "0.26.0"
    groovy
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
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.ocpsoft.prettytime:prettytime:$prettytimeVersion")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.12")
    testImplementation("org.codehaus.groovy:groovy-all:2.4.11")
    testImplementation("org.spockframework:spock-core:1.1-groovy-2.4")
}

tasks.test {
    useJUnitPlatform()
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

tasks.docker {
    dependsOn(tasks.distTar)
}
