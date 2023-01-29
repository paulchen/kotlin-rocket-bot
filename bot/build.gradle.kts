import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val reflectionsVersion = "0.10.2"
val coroutinesVersion = "1.6.4"
val ktorVersion = "2.2.2"
val jacksonVersion = "2.14.0"

plugins {
    id("kotlin-rocket-bot.conventions")
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    application
    id("com.palantir.docker") version "0.34.0"
    groovy
    id("org.openapi.generator") version "6.2.1"
    id("com.github.ben-manes.versions") version "0.44.0"
    id("jacoco")
    id("org.sonarqube") version "3.5.0.2730"
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current"
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.toLowerCase().contains("alpha") || candidate.version.toLowerCase().contains("beta")
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

    implementation("at.rueckgr.kotlin.rocketbot:kotlin-rocket-lib:1.0-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("at.favre.lib:bcrypt:0.9.0")

    implementation("org.postgresql:postgresql:42.5.1")
    implementation("org.ktorm:ktorm-core:3.5.0")
    implementation("org.ktorm:ktorm-support-postgresql:3.5.0")

    // dependencies for generated OpenAPI client
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.apache.groovy:groovy-all:4.0.7")
    testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.apache.commons:commons-io:1.3.2")
    testImplementation("org.ktorm:ktorm-support-mysql:3.5.0")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("org.assertj:assertj-core:3.24.2")
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
    )
}

distributions {
    main {
        version = "latest"
    }
}

docker {
    name = "${project.parent!!.name}:latest"
    files("build/distributions")
    noCache(true)
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

tasks.dockerPrepare {
    dependsOn(tasks.build)
}

openApiValidate {
    inputSpec.set("$projectDir/src/main/resources/openapi/api-football.yaml")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi/api-football.yaml")
    validateSpec.set(true)
    logToStderr.set(true)
    outputDir.set("$buildDir/generated/openapi")
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
