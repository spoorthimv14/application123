# SmartUrban Complaint System

A complete, production-quality civic technology application for urban complaint management.

## Project Architecture

```
Android Java/XML Application
          |
          | HTTP/JSON REST API
          v
Spring Boot Backend
          |
          | JPA/Hibernate
          v
MySQL Database
```

## Project Structure

```
SmartUrbanComplaintSystem/
    backend/
        pom.xml
        mvnw
        mvnw.cmd
        src/
            main/
                java/
                resources/
            test/
    android-app/
        app/
        build.gradle
        settings.gradle
        AndroidManifest.xml
    README.md
    .gitignore
```

## Getting Started

### Prerequisites
- JDK 17 / JDK 21+
- Android Studio / Android SDK (API 34)
- MySQL Server 8.0+

### Running Backend
```bash
cd backend
./mvnw clean spring-boot:run
```

### Running Android App
Open `android-app` in Android Studio or build using Gradle:
```bash
cd android-app
./gradlew assembleDebug
```
