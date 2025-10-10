plugins {
    id("java")
}

group = "edu.pitt.egm22"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("commons-io:commons-io:2.20.0")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:4.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}