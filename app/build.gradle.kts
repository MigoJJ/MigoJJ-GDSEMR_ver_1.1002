plugins {
    application
    alias(libs.plugins.javafx)
}

val javafxVersion: String by project
val sqliteVersion: String by project
val slf4jVersion: String by project

dependencies {
    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1") // Added for JSON parsing

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set("com.emr.gds.IttiaApp")
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics", "--enable-native-access=ALL-UNNAMED")
}