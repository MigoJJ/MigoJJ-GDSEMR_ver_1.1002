import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    alias(libs.plugins.spring.boot)
    java
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Explicit launcher needed for Gradle 9 test runner compatibility.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    // Matches the root Java 25 toolchain. Requires Spring Boot >= 3.5 — older
    // Spring Framework ASM versions can't parse Java 25 class files during
    // @SpringBootTest classpath scanning (BeanDefinitionStoreException: "Incompatible
    // class format" / ClassFormatException from ASM).
    options.release.set(25)
}
