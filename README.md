# xpdfmerge
 
Voraussetzung ist, dass das JDK (ab Version 8) installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Ich verwende zur Zeit JDK 11.

Wichtig ist, dass sich die Programmdatei Java, z.B. in der Konsole, ausführen lässt. Damit können generell Java-Programme ausgeführt werden.

Dazu muss die Umgebungsvariable _JAVA_HOME_ angelegt und auf den Pfad des JDK gesetzt werden, z.B. _C:\Program Files\RedHat\java-11-openjdk-11.0.9-3\_

Aufgrund der Abhängigkeit zu JavaFx muss der Verzeichnispfad der JavaFx-Lib-Dateien beim Aufruf angegeben werden. Da diese Dateien Teil des Gesamtpakets ist, kommt es darauf an, dass das richtige Unterverzeichnis angegeben wird.

Damit dieser Pfad beim Start des Java-Launchers nicht jedes Mal angegeben werden muss, habe ich eine kleine PowerShell-Skriptdatei angelegt.

Es kann natürlich genauso eine Stapeldatei oder eine Sh-Skriptdatei verwendet. Es werden immer zwei Schritte durchgeführt:

1) Setzen der Umgebungsvariablen PATH_TO_FX
2) Starten von java mit den erforderlichen Angaben.

Unter MacOS und Linux muss die PowerShell in der Regel installiert werden, was schnell und unkompliziert möglich sein sollte: https://github.com/PowerShell/PowerShell

Die für die Ausführung von xpdfmerge benötigten Dateien befinden sich in einer Zip-Datei.

https://github.com/pemo11/xpdfmerge/blob/main/xpdfMerge_Stand1011A.zip

**Wichtig:** Die Zip-Datei enthält NICHT die JavaFx-Dateien und nicht das Java 11-SDK.

Bei den JavaFx-Dateien kommt es (natürlich) darauf an, das sie zum OS passen müssen (Windows, MacOS und Linux).

Die einzelnen Schritte, um das Programm ausführen zu können:

**Schritt 1**: Alle Dateien in ein leeres Verzeichnis kopieren (klar)

**Schritt 2**: Die Zip-Datei javaFx.zip in das Verzeichnis mit den Dateien extrahieren, so dass es dort ein Unterverzeichnis JavaFx gibt.

**Schritt 3**: Start der Ps1-Datei, z.B. .\start.ps1 (bzw. setzen der Umgebungsvariablen und Ausführen von java, um die jar-Datei zu starten)

Dann sollte das Fenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann hoffentlich selbsterklärend.

**Hinweis:**

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet.

Über die Datei Log4j2.xml kann der Logger konfiguriert werden (noch nicht getestet, da es anscheinend auch ohne die Xml_Datei funktioniert).
