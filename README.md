# xpdfmerge
 
Voraussetzung ist, dass das JDK ab Version 8 installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Ich verwende zur Zeit die Version 11. Es sollte aber auch mit aktuelleren Versionen funktionieren.

Wichtig ist, dass sich die Programmdatei Java, z.B. in der Konsole, ausführen lässt. Damit können generell Java-Programme ausgeführt werden.

Dazu muss die Umgebungsvariable JAVA_HOME angelegt und auf den Pfad des JDK gesetzt werden, z.B. C:\Program Files\RedHat\java-11-openjdk-11.0.9-3\

Aufgrund der Abhängigkeit zu JavaFx muss der Verzeichnispfad der JavaFx-Lib-Dateien beim Aufruf angegeben werden. Da diese Dateien Teil des Gesamtpakets ist, kommt es darauf an, dass das richtige Unterverzeichnis angegeben wird.

Damit dieser Pfad beim Start des Java-Launchers nicht jedes Mal angegeben werden muss, habe ich eine kleine PowerShell-Skriptdatei angelegt.

Es muss natürlich kein PowerShell sein, es würde genauso mit einer Cmd- oder Sh-Skriptdatei funktionieren (nur müsste dann die Schreiwbeise in der Datei entsprechend angepasst werden)

Unter MacOs und Linux muss die PowerShell in der Regel installiert werden, was schnell und unkompliziert möglich sein sollte:

https://github.com/PowerShell/PowerShell

Alle für die Ausführung benötigten Dateien, befinden sich in einer Zip-Datei.

<<< Der DOWNLOAD LINK FOLGT NOCH >>>

Die einzelnen Schritte, um das Programm ausführen zu können:

 Schritt 1: Alle Dateien in ein leeres Verzeichnis kopieren (klar)

 Schritt 2: Sie müssen die Zip-Datei javaFx.zip in dem Verzeichnis mit den Dateien extrahieren, so dass es dort ein Verzeichnis JavaFx gibt.

 Schritt 3: Start der Ps1-Datei, z.B. .\start.ps1

Dann sollte das Fenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann hoffentlich selbsterklärend.

Hinweis:

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet.