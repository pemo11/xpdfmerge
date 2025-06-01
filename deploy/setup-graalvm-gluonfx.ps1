# ---------------------------------------------
# PowerShell-Skript zur Einrichtung von GraalVM und Kompilierung per GluonFX
# für macOS mit Apple Silicon (ARM64) und Java 17
# ---------------------------------------------

# Konfigurierbare Parameter
$graalVersion = "17.0.9"
$graalBaseUrl = "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-$graalVersion"
$graalFile = "graalvm-community-jdk-${graalVersion}_macos-aarch64_bin.tar.gz"
$installDir = "$HOME/graalvm"
$graalDir = "$installDir/graalvm-community-openjdk-17.0.9+9.1/Contents/Home"
$env:GRAALVM_HOME = $graalDir

# Sicherstellen, dass das Zielverzeichnis existiert
if (!(Test-Path -Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir | Out-Null
}

# Wechsel ins Zielverzeichnis
Set-Location -Path $installDir

# GraalVM herunterladen, wenn nicht vorhanden
if (!(Test-Path -Path "$installDir/$graalFile")) {
    Write-Host "🔽 Lade GraalVM $graalVersion herunter..."
    Invoke-WebRequest -Uri "$graalBaseUrl/$graalFile" -OutFile $graalFile
} else {
    Write-Host "✅ $graalFile bereits vorhanden"
}

# Entpacken, falls noch nicht geschehen
if (!(Test-Path -Path $graalDir)) {
    Write-Host "📦 Entpacke GraalVM..."
    tar -xzf $graalFile
} else {
    Write-Host "✅ GraalVM-Verzeichnis bereits entpackt"
}

# GRAALVM_HOME für diese Session setzen
$env:GRAALVM_HOME = $graalDir
$env:PATH = "$env:GRAALVM_HOME/bin:$env:PATH"

# Ausgabe der Version zur Kontrolle
$gralVersion = & "$env:GRAALVM_HOME/bin/java" -version 
Write-Host "📌 GraalVM-Version: $gralVersion"

# Wechsel ins Projektverzeichnis (hier ggf. anpassen!)
Set-Location "$PSScriptRoot"

# Maven-Build starten
Write-Host "🚀 Starte mvn gluonfx:compile..."
mvn gluonfx:compile
