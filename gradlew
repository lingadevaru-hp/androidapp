#!/bin/sh

# Gradle Wrapper for Gradle 9.3.1
# Simple version that works with Java 17

# Get the directory where this script is located
APP_HOME=$(cd "$(dirname "$0")" && pwd)

# Find Java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVACMD=$JAVA_HOME/bin/java
else
    JAVACMD=java
fi

# Check Java is available
if ! command -v $JAVACMD >/dev/null 2>&1; then
    echo "ERROR: Java not found. Please install Java 8 or higher."
    exit 1
fi

# Set classpath to gradle wrapper jar
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Execute Gradle
exec "$JAVACMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
