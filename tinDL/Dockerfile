FROM gradle:8.12.1-jdk17-noble AS build
WORKDIR /tin
# Copy the Gradle project files
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
RUN gradle dependencies --no-daemon

# Copy the application source code
COPY --chown=gradle:gradle src src

# Build the application
RUN gradle build --no-daemon -x test
RUN gradle bootJar --no-daemon -x test


# Use a minimal base image for running the application
FROM amazoncorretto:17-alpine3.21-full AS runtime

# Set the working directory
WORKDIR /build
COPY --from=build /tin ./

WORKDIR /tin
# Copy the built JAR from the build stage

COPY --from=build /tin/build/libs/*.jar app.jar
# Expose the application port
EXPOSE 8901

# Run the application
CMD ["java", "-jar", "app.jar"]