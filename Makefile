.PHONY: build checkstyle test

run-dist:
	./build/install/java-project-99/bin/java-project-99

build:
	./gradlew clean build

checkstyle:
	./gradlew checkstyleMain

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain checkstyleTest