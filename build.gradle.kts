import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.3" apply false
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
}

java.sourceCompatibility = JavaVersion.VERSION_17

allprojects {
    group = "dev.shefer"
	version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}


subprojects {

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

}
