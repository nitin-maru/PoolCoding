plugins {
    kotlin("jvm") version "1.9.23"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.json:json:20231013")

    //Hikari
    implementation("com.zaxxer:HikariCP:5.0.0")
    // HTTP client
    implementation ("io.ktor:ktor-client-core:2.3.12")
    implementation ("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation ("io.ktor:ktor-client-json:2.0.0")

    // JSON serialization
    implementation ("io.ktor:ktor-client-serialization:2.0.0")
    implementation ("io.ktor:ktor-server-content-negotiation:2.0.0")

    // Ktor Server Core
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")

    // Ktor Serialization
    implementation ("io.ktor:ktor-serialization:2.0.0")

    // Ktor Client (optional, if needed for other purposes)
//    implementation ("io.ktor:ktor-client-core:2.0.0")
//    implementation ("io.ktor:ktor-client-cio:2.0.0")

    // Postgres driver
    implementation ("org.postgresql:postgresql:42.7.2")

    // Bitcoin RPC
    implementation ("org.bitcoinj:bitcoinj-core:0.16.3")

    // Logging
    implementation ("ch.qos.logback:logback-classic:1.4.12")

    // Testing dependencies
    testImplementation ("org.jetbrains.kotlin:kotlin-test-junit:1.6.10")
    testImplementation ("io.ktor:ktor-client-mock:2.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}