repositories {
	mavenCentral()
}

plugins {
	java
	id("org.springframework.boot") version "3.5.9"
	id("io.spring.dependency-management") version "1.1.7"

	application
	checkstyle
	jacoco
	id("com.github.ben-manes.versions") version "0.52.0"
	id("org.sonarqube") version "7.2.2.6593"
	id("io.freefair.lombok") version "8.14"
	id("io.sentry.jvm.gradle") version "6.1.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"
description = "Task Manager for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("net.javacrumbs.json-unit:json-unit-assertj:3.2.2")
	implementation("net.datafaker:datafaker:2.0.1")
	implementation("org.instancio:instancio-junit:3.3.0")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	//MapStruct
	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

	implementation("org.openapitools:jackson-databind-nullable:0.2.8")


	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("com.h2database:h2:2.3.232")
	implementation("org.postgresql:postgresql:42.7.3")

	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	testImplementation("org.springframework.security:spring-security-test")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.16")
	implementation("io.sentry:sentry-spring-boot-starter-jakarta:8.33.0")
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

sentry {
	// Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
	// This enables source context, allowing you to see your source
	// code as part of your stack traces in Sentry.
	includeSourceContext = true

	org = "lolly-3r"
	projectName = "java-spring-boot2"
	authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "failed", "skipped", "standard_error", "standard_out")
		exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true
	}
}

tasks.jacocoTestReport { reports { xml.required.set(true) } }

tasks.withType<Test> {
	useJUnitPlatform()
}

application {
	mainClass = "hexlet.code.AppApplication"
}
