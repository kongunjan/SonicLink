#!/bin/bash
# Simple build script - no Maven/Gradle needed.
# Compiles everything under src/main/java into an out/ directory.

set -e
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
rm sources.txt
echo "Build complete. Class files are in out/"
echo "Run with: java -cp out com.soniclink.cli.SonicLinkCLI send \"Hello\""
