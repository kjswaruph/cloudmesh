plugins {
    id("java")
}

group = "app.cmesh"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Azure SDK
    implementation(platform("com.azure:azure-sdk-bom:1.3.3"))
    implementation("com.azure:azure-identity")
    implementation("com.azure:azure-storage-blob")
    implementation("com.azure.resourcemanager:azure-resourcemanager")
    implementation("com.azure.resourcemanager:azure-resourcemanager-compute")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}