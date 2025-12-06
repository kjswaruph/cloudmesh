plugins {
    id("java")
}

group = "app.cmesh"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Jackson for JSON
    implementation("tools.jackson.core:jackson-databind:3.0.3")
    implementation("tools.jackson.datatype:jackson-datatype-jsr310:3.0.0-rc2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:3.0-rc5")

    // Logging
    compileOnly("org.slf4j:slf4j-api:2.0.16")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}