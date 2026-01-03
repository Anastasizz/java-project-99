plugins {
	java
	id("org.springframework.boot") version "3.5.9"
	id("io.spring.dependency-management") version "1.1.7"

	application
	checkstyle
	jacoco
	id("com.github.ben-manes.versions") version "0.52.0"
	id("org.sonarqube") version "7.2.2.6593"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"
description = "Task Manager for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-web")
}

sonar {
	properties {
		property("sonar.projectKey", "Anastasizz_java-project-99")
		property("sonar.organization", "anastasizz")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")
		property("sonar.java.binaries", "build/classes/java/main")
		property("sonar.sources", "src/main/java")
		property("sonar.tests", "src/test/java")
	}
}

tasks.jacocoTestReport { reports { xml.required.set(true) } }

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
	mainClass = "hexlet.code.AppApplication"
}
