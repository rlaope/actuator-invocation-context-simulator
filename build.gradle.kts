plugins {
    kotlin("jvm") version "1.9.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-actuator:3.2.5")
}

application {
    mainClass.set("com.example.NullSafetyCheckKt")
}

kotlin {
    jvmToolchain(11)
}
