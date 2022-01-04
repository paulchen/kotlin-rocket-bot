import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val reflectionsVersion = "0.10.2"
val coroutinesVersion = "1.6.0"
val ktorVersion = "1.6.7"
val jacksonVersion = "2.13.1"

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("com.palantir.docker") version "0.31.0"
    groovy
}

group = "at.rueckgr.kotlin.rocketbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

sourceSets {
    main {
        resources {
            srcDirs("build/generated/resources")
        }
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("at.rueckgr.kotlin.rocketbot:kotlin-rocket-lib:1.0-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("at.favre.lib:bcrypt:0.9.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.10")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<GroovyCompile> {
    targetCompatibility = "17"
}

application {
    mainClass.set("at.rueckgr.kotlin.rocketbot.MainKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true",
    )
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
        val file = File("build/generated/resources/git-revision")
        project.mkdir(file.parentFile.path)
        file.delete()

        file.appendText(String.format("revision = %s\n", runGit("git", "rev-parse", "--short", "HEAD")))
        file.appendText(String.format("commitMessage = %s\n", runGit("git", "log", "-1", "--pretty=%B")))
    }
}

fun runGit(vararg args: String): String {
    val outputStream = ByteArrayOutputStream()
    project.exec {
        commandLine(*args)
        standardOutput = outputStream
    }
    return outputStream.toString().split("\n")[0].trim()
}

tasks.processResources {
    dependsOn("createVersionFile")
}

tasks.dockerPrepare {
    dependsOn(tasks.build)
}
