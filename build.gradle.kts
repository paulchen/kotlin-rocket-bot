import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val reflectionsVersion = "0.10.1"
val coroutinesVersion = "1.5.2"
val ktorVersion = "1.6.4"

plugins {
    kotlin("jvm") version "1.5.31"
    application
    id("com.palantir.docker") version "0.30.0"
    groovy
    id("com.palantir.git-version") version "0.12.3"
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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("at.rueckgr.kotlin.rocketbot.MainKt")
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

tasks.dockerPrepare {
    dependsOn(tasks.build)
}
