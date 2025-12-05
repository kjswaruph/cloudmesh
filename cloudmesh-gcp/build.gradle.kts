plugins {
    id("java")
}

group = "app.cmesh.gcp"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // GCP SDK
    implementation(platform("com.google.cloud:libraries-bom:26.72.0"))
    implementation("com.google.cloud:google-cloud-compute")
    implementation("com.google.cloud:google-cloud-storage")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.40.0")

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