plugins {
    id("kotlin-rocket-bot.conventions")
    antlr
    id("com.github.ben-manes.versions") version "0.46.0"
}

dependencies {
    antlr("org.antlr:antlr4:4.12.0")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
