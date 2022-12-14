import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.0"
    // Need snapshot version for https://github.com/SpongePowered/VanillaGradle/issues/72
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "me.techchrism"
version = "1.2.1"

repositories {
    mavenCentral()
}

dependencies {
    shadow(kotlin("stdlib-jdk8"))
    shadow("net.md-5:SpecialSource:1.11.0")
    shadow("com.google.code.gson:gson:2.9.0")
}

minecraft {
    version("1.19")
    platform(org.spongepowered.gradle.vanilla.repository.MinecraftPlatform.SERVER)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "me.techchrism.enchantmentjsonexporter.CommandLine"
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.shadow.get())
    }

    named("build"){
        dependsOn(shadowJar)
    }
}
