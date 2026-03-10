#!/bin/bash
set -e

echo "========================================="
echo "  Library Management API - Build Start  "
echo "========================================="

# Find javac
if ! command -v javac &> /dev/null; then
  echo "ERROR: javac not found. Please ensure Java JDK is installed."
  exit 1
fi

echo "Java version:"
javac -version 2>&1

echo "Compiling Java source files..."
mkdir -p out
javac -d out src/*.java

echo "Compilation successful. Starting server..."
cd out
java -cp . Main
