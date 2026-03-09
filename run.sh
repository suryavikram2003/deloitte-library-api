#!/bin/bash
echo "Compiling Java files..."
javac src/*.java -d .
if [ $? -eq 0 ]; then
  echo "Compilation successful. Starting server..."
  java Main
else
  echo "Compilation failed. Please check errors above."
  exit 1
fi
