# PowerShell: build-with-jpackage-final-final.ps1
# Ziel: .dmg mit vollständig integrierter JavaFX-Runtime aus JavaFx/lib

# Konfiguration
$AppName = "XEFPdfMerge"
$MainJar = "pdfmergev1-1.0-SNAPSHOT-with-dependencies.jar"
$MainClass = "xpdfmergeV1.XEFPdfMerge"
$IconPath = "xpdfmerge.icns"
$InputDir = "target"
$OutputDir = "target/dist"
$JavaFxLib = "JavaFx/lib"

# Zielordner anlegen
if (!(Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

# JavaFX-JARs ins target/ kopieren
Write-Host "📦 Kopiere JavaFX-Dateien aus $JavaFxLib nach $InputDir..."
Get-ChildItem "$JavaFxLib/*.jar" | ForEach-Object {
    Copy-Item $_.FullName -Destination $InputDir -Force
}

# jpackage ausführen
Write-Host "🚀 Starte jpackage mit JavaFX..."
jpackage `
    --type dmg `
    --name $AppName `
    --input $InputDir `
    --main-jar $MainJar `
    --main-class $MainClass `
    --icon $IconPath `
    --dest $OutputDir `
    --java-options "--module-path . --add-modules javafx.controls,javafx.fxml -Dfile.encoding=UTF-8"

# Pfad zur App im .dmg prüfen
$AppPath = "$OutputDir/$AppName.app/Contents/MacOS/$AppName"
if (Test-Path $AppPath) {
    Write-Host "🔧 Setze Ausführungsrechte für $AppPath..."
    chmod +x $AppPath
    Write-Host "🧼 Entferne macOS-Quarantäneflag..."
    xattr -d com.apple.quarantine "$OutputDir/$AppName.app"
}

Write-Host "✅ $AppName wurde erfolgreich als .dmg erstellt und vorbereitet für den Start."