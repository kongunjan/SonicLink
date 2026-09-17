#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "             Building SonicLink"
echo "=========================================="

rm -rf out
mkdir -p out

SOURCES_LIST="sources.tmp"

find src/main/java -name "*.java" | sort > "$SOURCES_LIST"

if [ -d "src/test/java" ]; then
    find src/test/java -name "*.java" | sort >> "$SOURCES_LIST"
fi

SOURCE_COUNT=$(wc -l < "$SOURCES_LIST" | tr -d ' ')

echo "Compiling $SOURCE_COUNT Java source files..."
echo ""

javac -encoding UTF-8 -d out @"$SOURCES_LIST"

COMPILE_STATUS=$?

rm -f "$SOURCES_LIST"

if [ $COMPILE_STATUS -ne 0 ]; then

    echo ""
    echo "=========================================="
    echo "✗ BUILD FAILED"
    echo "=========================================="

    exit $COMPILE_STATUS
fi

echo ""
echo "✓ Build complete."
echo "✓ Class files located in out/"
echo "=========================================="

if [ "$1" = "test" ]; then

    echo ""
    echo "Running SonicLink test suite..."
    echo ""

    java -cp out com.soniclink.AllTestsRunner

    exit $?
fi

if [ "$1" = "run" ]; then

    if [ -z "$2" ]; then

        echo ""
        echo "✗ No command specified."
        echo ""
        echo "Usage:"
        echo "  bash build.sh run send \"Hello SonicLink\""
        echo "  bash build.sh run send-file ./test.txt"
        echo "  bash build.sh run send-file ./report.pdf"
        echo "  bash build.sh run send-file ./image.png"
        echo "  bash build.sh run listen"
        echo "  bash build.sh run self-test"
        echo "  bash build.sh run help"

        exit 1
    fi

    shift

    java -cp out com.soniclink.cli.SonicLinkCLI "$@"

    exit $?
fi

if [ "$1" = "clean" ]; then

    rm -rf out

    echo ""
    echo "✓ Build directory cleaned."

    exit 0
fi

echo ""
echo "SonicLink Build System"
echo ""
echo "Usage:"
echo ""
echo "  bash build.sh test"
echo "      Build and run tests"
echo ""
echo "  bash build.sh run <command>"
echo "      Build and run SonicLink"
echo ""
echo "Commands:"
echo ""
echo "  send <message>"
echo "  send-file <path>"
echo "  listen"
echo "  self-test"
echo "  help"
echo ""
echo "Examples:"
echo ""
echo "  bash build.sh run send \"Hello SonicLink\""
echo "  bash build.sh run send-file ./test.txt"
echo "  bash build.sh run send-file ./report.pdf"
echo "  bash build.sh run send-file ./notes.png"
echo "  bash build.sh run listen"
echo ""