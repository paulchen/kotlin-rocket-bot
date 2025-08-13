import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files

val reflectionsVersion = "0.10.2"
val coroutinesVersion = "1.10.2"
val ktorVersion = "3.2.3"
val jacksonVersion = "2.19.2"
val ktormVersion = "4.1.1"
val kotlinVersion = "2.2.0"
val okhttpVersion = "4.12.0"

plugins {
    id("kotlin-rocket-bot.conventions")
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
    groovy
    id("org.openapi.generator") version "7.14.0"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("jacoco")
    id("org.sonarqube") version "6.2.0.5505"
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

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("at.rueckgr.kotlin.rocketbot:kotlin-rocket-lib:0.1.7-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("org.apache.commons:commons-text:1.14.0")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.kohlschutter.junixsocket:junixsocket-core:2.10.1")
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-support-postgresql:$ktormVersion")

    implementation("de.focus-shift:jollyday-jaxb:1.5.6")

    // dependencies for generated OpenAPI client
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.moshi:moshi-adapters:1.15.2")
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.apache.groovy:groovy-all:4.0.28")
    testImplementation("org.spockframework:spock-core:2.4-M6-groovy-4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttpVersion")
    testImplementation("commons-io:commons-io:2.20.0")
    testImplementation("org.ktorm:ktorm-support-mysql:$ktormVersion")
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.assertj:assertj-core:4.0.0-M1")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
    }
    dependsOn("openApiGenerate")
}

tasks.withType<GroovyCompile> {
    targetCompatibility = "21"
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

tasks.register("createVersionFile") {
    doLast {
        val file = file("build/generated/resources/git-revision")
        Files.createDirectories(file.parentFile.toPath())
        Files.deleteIfExists(file.toPath())

        file.appendText(String.format("revision = %s\n", runGit("git", "rev-parse", "--short", "HEAD")))
        file.appendText(String.format("commitMessage = %s\n", runGit("git", "log", "-1", "--pretty=%B")))
    }
}

fun runGit(vararg args: String) =
    project
        .providers.exec {
            commandLine(*args)
        }
        .standardOutput.asText.get()
        .split("\n")[0].trim()

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
