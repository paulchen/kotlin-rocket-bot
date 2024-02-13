import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val reflectionsVersion = "0.10.2"
val coroutinesVersion = "1.7.3"
val ktorVersion = "2.3.8"
val jacksonVersion = "2.16.1"
val ktormVersion = "3.6.0"
val okhttpVersion = "4.12.0"

plugins {
    id("kotlin-rocket-bot.conventions")
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
    groovy
    id("org.openapi.generator") version "7.3.0"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("jacoco")
    id("org.sonarqube") version "4.4.1.3373"
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current"
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.lowercase().contains("alpha") ||
                candidate.version.lowercase().contains("beta") ||
                candidate.version.lowercase().contains("rc")
    }
}

sourceSets {
    main {
        java {
            srcDirs(
                "src/main/kotlin",
                "build/generated/openapi/src/main/kotlin"
            )
        }
        resources {
            srcDirs("build/generated/resources")
        }
    }
}

dependencies {
    implementation(project(":unicode"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("at.rueckgr.kotlin.rocketbot:kotlin-rocket-lib:0.1.5-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-support-postgresql:$ktormVersion")

    implementation("de.focus-shift:jollyday-jaxb:0.25.0")

    // dependencies for generated OpenAPI client
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.moshi:moshi-adapters:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.apache.groovy:groovy-all:4.0.18")
    testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("org.apache.commons:commons-io:1.3.2")
    testImplementation("org.ktorm:ktorm-support-mysql:$ktormVersion")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    dependsOn("openApiGenerate")
}

tasks.withType<GroovyCompile> {
    targetCompatibility = "17"
}

tasks.named<GroovyCompile>("compileTestGroovy") {
    classpath += files(tasks.compileTestKotlin)
}

application {
    mainClass.set("at.rueckgr.kotlin.rocketbot.MainKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true",
        "-agentlib:jdwp=transport=dt_socket,server=y,address=*:5005,suspend=n",
	"-Dcom.sun.management.jmxremote=true",
	"-Dcom.sun.management.jmxremote.ssl=false",
	"-Dcom.sun.management.jmxremote.authenticate=false",
	"-Dcom.sun.management.jmxremote.port=9010",
	"-Djava.rmi.server.hostname=localhost",
	"-Dcom.sun.management.jmxremote.rmi.port=9011"
    )
}

distributions {
    main {
        version = "latest"
    }
}

tasks.create("createVersionFile") {
    doLast {
        val file = File("$projectDir/build/generated/resources/git-revision")
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

openApiValidate {
    inputSpec.set("$projectDir/src/main/resources/openapi/api-football.yaml")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/api-football.yaml")
    validateSpec.set(true)
    logToStderr.set(true)
    outputDir.set("${layout.buildDirectory.get()}/generated/openapi")
    packageName.set("com.api_football")
    configOptions.put("dateLibrary", "java8")
    globalProperties.put("modelDocs", "false")
    typeMappings.put("datetime", "DateTime")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("com/api_football/**")
            }
        })
    )
}
