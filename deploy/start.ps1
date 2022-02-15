<#
 .Synopsis
 Startet die Java-App xpdfmerge über die Ausführung von Java
 .Description
 Aktuell muss auch das JavaFx-SDK installiert sein, z.B. D:\Java11\JavaFx
 Die Datei app.config muss sich im aktuellen Verzeichnis befinden
#>

# Anlegen einer Umgebungsvariablen PATH_TO_FX für den java-Process

# new-item -path env: -name PATH_TO_FX -value D:\Java11\JavaFx\Lib -Force
# new-item -path env: -name PATH_TO_FX -value G:\2021\xpdfmerge\out\artifacts\pdfmergev1_jar\JavaFx\Lib -Force
# new-item -path env: -name PATH_TO_FX -value $PSScriptRoot\JavaFx\Lib -Force
$FxPfad = "/Library/Java/javafx-sdk-11.0.2/lib"
new-item -path env: -name PATH_TO_FX -value $FxPfad -Force

# Jar-Datei ausführen
java --module-path $env:PATH_TO_FX --add-modules javafx.controls -jar pdfmergev1.jar
