plugins {
    `kotlin-dsl`
    id("groovy-gradle-plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("app.cash.licensee:licensee-gradle-plugin:1.9.1")
}

