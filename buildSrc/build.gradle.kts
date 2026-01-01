plugins {
    `kotlin-dsl`
    kotlin("jvm") version "2.3.0"
    id("groovy-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("app.cash.licensee:licensee-gradle-plugin:1.9.1")
}

