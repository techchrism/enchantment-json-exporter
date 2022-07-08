import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    // Need snapshot version for https://github.com/SpongePowered/VanillaGradle/issues/72
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

group = "me.techchrism"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

minecraft {
    version("1.19")
    runs {
        server()
    }
}
