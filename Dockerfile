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
