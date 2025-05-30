#!/bin/bash

# Clear the output directory
echo "Cleaning output directory..."
rm -rf out/*

# Compile common classes
echo "Compiling common classes..."
if ! javac -d out src/common/*.java; then
    echo "Error compiling common classes"
    exit 1
fi

# Compile server classes
echo "Compiling server classes..."
if ! javac -cp out -d out src/server/*.java; then
    echo "Error compiling server classes"
    exit 1
fi

# Compile client classes
echo "Compiling client classes..."
if ! javac -cp out -d out src/client/*.java; then
    echo "Error compiling client classes"
    exit 1
fi

echo "Compilation completed successfully!"
