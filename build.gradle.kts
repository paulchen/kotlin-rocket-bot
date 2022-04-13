import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

val reflectionsVersion = "0.10.2"
val coroutinesVersion = "1.6.0"
val ktorVersion = "1.6.8"
val jacksonVersion = "2.13.2"

plugins {
    kotlin("jvm") version "1.6.20"
    application
    id("com.palantir.docker") version "0.33.0"
    groovy
    id("org.openapi.generator") version "5.4.0"
    id("com.github.ben-manes.versions") version "0.42.0"
}

group = "at.rueckgr.kotlin.rocketbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("at.rueckgr.kotlin.rocketbot:kotlin-rocket-lib:1.0-SNAPSHOT")

    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    // CVE-2020-36518 - remove when updating to 2.13.3 or 2.14.0
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("at.favre.lib:bcrypt:0.9.0")

    implementation("org.postgresql:postgresql:42.3.3")
    implementation("org.ktorm:ktorm-core:3.4.1")
    implementation("org.ktorm:ktorm-support-postgresql:3.4.1")

    // dependencies for generated OpenAPI client
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
    implementation("com.squareup.moshi:moshi-adapters:1.13.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.20")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.10")
    testImplementation("org.spockframework:spock-core:2.1-groovy-3.0")
}

tasks.test {
    useJUnitPlatform()
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

openApiValidate {
    inputSpec.set("$rootDir/src/main/resources/openapi/api-football.yaml")
}

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/src/main/resources/openapi/api-football.yaml")
    validateSpec.set(true)
    logToStderr.set(true)
    outputDir.set("$buildDir/generated/openapi")
    packageName.set("com.api_football")
    configOptions.put("dateLibrary", "java8")
    globalProperties.put("modelDocs", "false")
    typeMappings.put("datetime", "DateTime")
}
