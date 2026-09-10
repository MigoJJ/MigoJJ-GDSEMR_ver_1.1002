// GDSEMR_ver_1.1001 - Gradle multi-module build
//
// Centralizes the Java toolchain setup and keeps module build files lean.

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

val javaVersion: String by project

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
            }
        }
    }
}

// 루트에서 `./gradlew run` 하면 app 모듈의 run 실행
tasks.register("run") {
    dependsOn(":app:run")
}