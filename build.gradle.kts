import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.asodesu.teamsilly"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    implementation("dev.asodesu.origami.engine:engine:0.0.1-alpha")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}
tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.runServer {
    minecraftVersion("1.21.1")
    systemProperty("paper.playerconnection.keepalive", 36000)
    systemProperty("paper.disablePluginRemapping", true)
    systemProperty("paper.disableOldApiSupport", true)
    if (project.hasProperty("hotswapping")) {
        jvmArgs("-XX:+AllowEnhancedClassRedefinition")
    }
}

bukkit {
    name = "SillyGame"
    version = project.version.toString()
    description = "TEAM SILLIES INVENTORY JAM ENTRY"
    website = "https://github.com/AsoDesu/Origami"
    author = "AsoDesu_"

    main = "dev.asodesu.teamsilly.SillyGamePlugin"
    apiVersion = "1.21"
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier = ""
    }
    jar {
        enabled = false
    }
}