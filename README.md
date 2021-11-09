# xpdfmerge
 
Voraussetzung ist, dass das JDK ab Version 8 installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Ich verwende die Version 11.

Wichtig ist, dass sich die Programmdatei Java später ausführen lässt. Dazu muss die Umgebungsvariable JAVA_HOME angelegt und auf den Pfad des JDK gesetzt werden, z.B. C:\Program Files\RedHat\java-11-openjdk-11.0.9-3\

Die PowerShell-Skriptdatei Start.ps1 kopiert ein paar Dateien und den Java-Compiler auf.

Sie kann natürlich auch durch eine Bat-,Cmd- oder Sh-Datei ersetzt werden. Grundsätzlich funktioniert sie auch unter MacOS und Linux, wenn dort die PowerShell installiert wurde (Versionsnummer der PowerShell spielt keine Rolle)

Alle für die Ausführung benötigten Dateien, befinden sich in einer Zip-Datei.

<<< Der DOWNLOAD LINK FOLGT NOCH >>>

Die einzelnen Schritte, um das Programm ausführen zu können:

 Schritt 1: Alle Dateien in ein leeres Verzeichnis kopieren (klar)

 Schritt 2: Sie müssen die Zip-Datei javaFx.zip in dem Verzeichnis mit den Dateien extrahieren, so dass es dort ein Verzeichnis JavaFx gibt.

 Schritt 3: Start der Ps1-Datei, z.B. .\start.ps1

Dann sollte das Fenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann selbsterklärend.

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet.