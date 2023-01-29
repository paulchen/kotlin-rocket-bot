plugins {
    id("kotlin-rocket-bot.conventions")
    antlr
}

dependencies {
    antlr("org.antlr:antlr4:4.11.1")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}
