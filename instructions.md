# DevOps Setup — **student-management** (No DB, Docker Compose, Jenkins, Sonar)

This guide sets up your Spring Boot app **without a database**, built and deployed via **Docker Compose** and automated by **Jenkins**. It also shows how to add **SonarQube** analysis. Everything runs on your Ubuntu host (no local Maven build required).

> ✅ You already have Docker installed.  
> ✅ We disable Spring’s DB auto‑configuration at runtime so the app can run **without MySQL**.  
> ✅ Build happens inside Docker (multi‑stage), not on your host.

---

## 1) Project layout (at repo root)

Add these files if missing:

```
.
├─ Dockerfile
├─ docker-compose.yml
├─ Jenkinsfile
└─ (your existing sources ...)
```

---

## 2) Dockerfile (multi‑stage, builds JAR inside Docker)

**`Dockerfile`**
```dockerfile
# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# Use Maven Wrapper inside the container
RUN chmod +x mvnw && ./mvnw -B -DskipTests clean package

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# App JAR
COPY --from=build /app/target/*.jar app.jar

# App runs on 8089 per your project notes
ENV SERVER_PORT=8089
EXPOSE 8089

# If the project expects a DB, disable JDBC/JPA auto-config at runtime
# so the app can start without any database.
ENV JAVA_TOOL_OPTIONS="-Dspring.autoconfigure.exclude=\
org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"

# Optional healthcheck (requires Spring Actuator). Comment out if you don't have actuator.
# HEALTHCHECK --interval=30s --timeout=5s --start-period=30s \
#   CMD wget -qO- http://localhost:${SERVER_PORT}/student/actuator/health | \
#   grep '\"status\":\"UP\"' || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
```

> **Why `JAVA_TOOL_OPTIONS`?** It injects a JVM system property that tells Spring Boot to **skip DB auto‑configuration**, allowing the app to boot **without** MySQL/Postgres even if those starters are on the classpath.

---

## 3) Docker Compose (app only; optional Sonar profile)

**`docker-compose.yml`**
```yaml
version: "3.9"

name: student-management-stack

services:
  app:
    build: .
    container_name: sm-app
    ports:
      - "8089:8089"
    environment:
      SERVER_PORT: "8089"
      # Keep the same exclusion here in case you run the JAR directly in the image
      JAVA_TOOL_OPTIONS: >-
        -Dspring.autoconfigure.exclude=
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration

  # Enable SonarQube with: docker compose --profile sonar up -d
  sonarqube:
    image: sonarqube:lts
    container_name: sm-sonarqube
    ports:
      - "9000:9000"
    profiles: ["sonar"]
```

### Run
```bash
# build image & start app
docker compose up -d --build

# logs
docker compose logs -f app

# stop
docker compose down
```

App URL: `http://localhost:8089/student/` (adjust if your controllers use different paths).

---

## 4) SonarQube (optional, local)

Start Sonar when you need a scan:
```bash
docker compose --profile sonar up -d
# open http://localhost:9000 → login admin/admin → change password
# Create project "student-management" → generate a token (SONAR_TOKEN)
```

**Maven plugin** (usually already present; add if missing in `pom.xml`):
```xml
<plugin>
  <groupId>org.sonarsource.scanner.maven</groupId>
  <artifactId>sonar-maven-plugin</artifactId>
  <version>3.10.0.2594</version>
</plugin>
```

Local scan (from your host or inside a build container):
```bash
./mvnw -B -DskipTests verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<SONAR_TOKEN>
```

---

## 5) Jenkins Pipeline (build image + compose deploy + Sonar)

**In Jenkins:**
1. *Manage Plugins* → ensure **Pipeline** and **SonarQube Scanner for Jenkins** are installed.
2. *Manage Jenkins → System → SonarQube Servers* → add a server:  
   - Name: `local-sonar`, URL: `http://localhost:9000`  
   - Credentials: add your **SONAR_TOKEN** (as *Secret Text*, ID `SONAR_TOKEN`).
3. (Optional) *Global Tool Configuration* → JDK 17 (if needed).

**`Jenkinsfile`**
```groovy
pipeline {
  agent any
  environment {
    SONAR_HOST_URL = 'http://localhost:9000'
    DOCKER_BUILDKIT = '1'
  }
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build JAR (inside Dockerfile)') {
      steps {
        // Nothing to do here on the host; the Dockerfile builds the JAR.
        echo 'The multi-stage Docker build will compile the project.'
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker compose build app'
      }
    }
    stage('SonarQube Analysis') {
      when { expression { return env.SONAR_HOST_URL != null } }
      steps {
        withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
          sh """
            chmod +x mvnw
            ./mvnw -B -DskipTests verify sonar:sonar \
              -Dsonar.host.url=${SONAR_HOST_URL} \
              -Dsonar.login=${SONAR_TOKEN}
          """
        }
      }
    }
    stage('Deploy with Compose') {
      steps {
        sh 'docker compose up -d app'
      }
    }
  }
  post {
    success {
      sh 'docker ps --format "{{.Names}} -> {{.Image}}"'
      echo 'Deployment complete.'
    }
  }
}
```

> This pipeline: **checkout → build image (which compiles the JAR) → optional Sonar scan → deploy via Compose**.  
> It does **not** require a local Maven installation; the build happens inside the Dockerfile’s first stage.

---

## 6) Validation checklist

- `docker compose ps` shows `sm-app` **Up**.  
- `curl http://localhost:8089/student/` (or a real controller path) returns a response.  
- Jenkins job succeeds and redeploys on new commits.  
- (Optional) SonarQube dashboard displays your project’s analysis.

---

## 7) Troubleshooting

- **Port 8089 in use** → change `SERVER_PORT` env or stop the other service.  
- **App still tries to connect to DB** → ensure `JAVA_TOOL_OPTIONS` is set as shown (both in Dockerfile and Compose).  
- **Sonar auth errors** → regenerate token; check Jenkins credential ID `SONAR_TOKEN`.  
- **No actuator endpoint** → comment out the healthcheck in the Dockerfile or add `spring-boot-starter-actuator`.