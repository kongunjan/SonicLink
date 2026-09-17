#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "         Building SonicLink               "
echo "=========================================="

# Create clean output directory
rm -rf out
mkdir -p out

# Collect all Java sources
SOURCES_LIST="sources.tmp"

find src/main/java -name "*.java" > "$SOURCES_LIST"

if [ -d "src/test/java" ]; then
    find src/test/java -name "*.java" >> "$SOURCES_LIST"
fi

SOURCE_COUNT=$(wc -l < "$SOURCES_LIST" | tr -d ' ')

echo "Compiling $SOURCE_COUNT Java source files..."

# Compile all Java sources
javac -d out @"$SOURCES_LIST"

rm -f "$SOURCES_LIST"

echo "✓ Build complete. Class files located in out/"
echo "=========================================="

# ------------------------------------------
# TEST
# ------------------------------------------

if [ "$1" == "test" ]; then

    echo ""
    echo "Running test suite..."

    java -cp out com.soniclink.AllTestsRunner

    exit $?
fi

# ------------------------------------------
# RUN
# ------------------------------------------

if [ "$1" == "run" ]; then

    if [ -z "$2" ]; then

        echo ""
        echo "✗ No command specified."
        echo ""
        echo "Usage:"
        echo "  bash build.sh run send \"Hello SonicLink\""
        echo "  bash build.sh run listen"
        echo "  bash build.sh run self-test"
        echo "  bash build.sh run help"

        exit 1
    fi

    shift

    java -cp out com.soniclink.cli.SonicLinkCLI "$@"

    exit $?
fi

# ------------------------------------------
# CLEAN
# ------------------------------------------

if [ "$1" == "clean" ]; then

    rm -rf out

    echo ""
    echo "✓ Build directory cleaned."

    exit 0
fi

# ------------------------------------------
# HELP / NO COMMAND
# ------------------------------------------

echo ""
echo "SonicLink Build System"
echo ""
echo "Usage:"
echo ""
echo "  bash build.sh test"
echo "      Build and run all tests"
echo ""
echo "  bash build.sh run <command>"
echo "      Build and run SonicLink"
echo ""
echo "Examples:"
echo ""
echo "  bash build.sh run send \"Hello SonicLink\""
echo "  bash build.sh run listen"
echo "  bash build.sh run self-test"
echo "  bash build.sh run help"
echo ""
echo "  bash build.sh clean"
echo "      Remove compiled files"
echo ""