#!/usr/bin/env bash
# SonicLink Build Script
# Compiles all main application and test classes without external build tools.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "         Building SonicLink               "
echo "=========================================="

# Create clean output directory
rm -rf out
mkdir -p out

# Collect all Java sources (main and test)
SOURCES_LIST="sources.tmp"
find src/main/java -name "*.java" > "$SOURCES_LIST"
if [ -d "src/test/java" ]; then
    find src/test/java -name "*.java" >> "$SOURCES_LIST"
fi

SOURCE_COUNT=$(wc -l < "$SOURCES_LIST" | tr -d ' ')
echo "Compiling $SOURCE_COUNT Java source files..."

# Compile with strict warnings
javac -d out @"$SOURCES_LIST"
rm -f "$SOURCES_LIST"

echo "✓ Build complete. Class files located in out/"
echo "=========================================="

if [ "$1" == "test" ]; then
    echo ""
    echo "Running test suite..."
    java -cp out com.soniclink.AllTestsRunner
fi
