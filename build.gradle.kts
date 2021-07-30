import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.6.1"
val coroutinesVersion = "1.3.8"
var commonsCodecVersion = "1.15"
val gsonVersion = "2.8.7"
val reflectionsVersion = "0.9.12"
val log4jVersion = "2.14.1"

plugins {
    kotlin("jvm") version "1.5.10"
    application
    id("com.palantir.docker") version "0.26.0"
    groovy
    id("com.palantir.git-version") version "0.12.3"
}

group = "at.rueckgr.kotlin.rocketbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "build/generated/resources")
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("commons-codec:commons-codec:${commonsCodecVersion}")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.8")
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
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

tasks.create("createVersionFile") {
    doLast {
        val gitVersion: groovy.lang.Closure<String> by project.extra
        val file = File("build/generated/resources/git-revision")
        project.mkdir(file.parentFile.path)
        file.delete()
        file.appendText(gitVersion())
    }
}

tasks.processResources {
    dependsOn("createVersionFile")
}

tasks.docker {
    dependsOn(tasks.distTar)
}
