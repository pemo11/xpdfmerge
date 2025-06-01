#!/bin/bash

# Launch script for XEFPdfMerge JavaFX application on macOS
# Created: May 28, 2025

# Get the directory of this script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set path to the JavaFX modules
JAVAFX_MODULES="$SCRIPT_DIR/deploy/javaFx/lib"

# Set the main JAR file - jetzt direkt aus dem deploy-Verzeichnis
JAR_FILE="$SCRIPT_DIR/deploy/pdfmergev1.jar"

# Set the main class
MAIN_CLASS="xpdfmergeV1.XEFPdfMerge"

# Run the application with proper JavaFX module path
java --module-path "$JAVAFX_MODULES" \
     --add-modules=javafx.controls,javafx.fxml,javafx.graphics \
     -cp "$JAR_FILE" \
     "$MAIN_CLASS"