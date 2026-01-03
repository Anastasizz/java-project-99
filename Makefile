.PHONY: build checkstyle test

run-dist:
	./build/install/app/bin/app

build:
	./gradlew build

checkstyle:
	./gradlew checkstyleMain

test:
	./gradlew test

report:
	./gradlew jacocoTestReport