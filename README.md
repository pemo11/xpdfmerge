# xpdfmerge
 
Voraussetzung ist, dass das JDK (ab Version 11) installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Theoretisch ist das Liberica SDK eine gute Alternative (https://bell-sw.com/pages/downloads/), da hier (bei Full SDK) JavaFX dabei ist - das habe ich aber noch nicht getestet. Auf der anderen Seite ist es flexibler, die JavaFx-Dateien beim Start der Jar-Datei auswählen zu können.

Generell gilt, dass JavaFX ab JDK 8 nicht mehr dabei ist und separat von https://openjfx.io/ heruntergeladen und in einem eigenen Verzeichnis abgelegt werden muss, dessen Pfad bei der Ausführung des Programms angegeben werden muss.

**Hinweis:** Hinter *JavaFX* steht die Firma *Gluon*, so dass man bei der Suche nach dem Stichwort JavaFX ab und zu direkt auf der Firmenwebseite (https://gluonhq.com/) landen kann.

Wichtig ist allgemein, dass sich die Programmdatei Java, z.B. in der Konsole, ausführen lässt. Damit können generell Java-Programme ausgeführt werden und die Umgebungsvariable *JAVA_HOME* enthält den richtigen Verzeichnispfad.

Wenn nicht, muss die Umgebungsvariable _JAVA_HOME_ angelegt und auf den Pfad des JDK gesetzt werden, z.B. *C:\Program Files\RedHat\java-11-openjdk-11.0.9-3*

Aufgrund der erwähnten Abhängigkeit des Programms zu JavaFX, muss der Verzeichnispfad der JavaFx-Lib-Dateien beim Aufruf angegeben werden. Da diese Dateien Teil des Gesamtpakets ist, kommt es darauf an, dass das richtige Unterverzeichnis angegeben wird.

Damit dieser Pfad beim Start des Java-Launchers nicht jedes Mal angegeben werden muss, habe ich eine kleine PowerShell-Skriptdatei angelegt.

Unter MacOS und Linux muss die PowerShell in der Regel installiert werden, was schnell und unkompliziert möglich sein sollte: https://github.com/PowerShell/PowerShell

Wer sich mit Stapeldateien oder allgemein Shellscripting auskennt, verwendet natürlich eine Stapeldatei oder ein kleines Bash-Skript oder was auch immer;)

Es werden in dem "Skript" zwei Schritte durchgeführt:

1) Setzen der Umgebungsvariablen *PATH_TO_FX* auf das lib-Unterverzeichnis im JavaFx-Verzeichnis
2) Starten von java mit den erforderlichen Angaben.

Die Programmdatei besteht (wie bei Java üblich) aus einer einzigen Jar-Datei:

https://github.com/pemo11/xpdfmerge/blob/main/deploy/pdfmergev1.jar

**Wichtig:** Das Java (11) SDK und die JavaFX-Dateien müssen separat installiert werden (am besten natürlich vor dem ersten Programmstart;).

Das kleine (und optionale) PowerShell-Skript ist Teil dieses Projektportals (und wird am einfachsten in der Raw-Ansicht kopiert und in eine lokale Textdatei eingefügt, die als Ps1-Datei lokal gespeichert wird).

Bei den JavaFX-Dateien kommt es (natürlich) darauf an, das sie zum OS passen müssen (Windows, MacOS und Linux).

Die einzelnen Schritte, um XpdfMerge ausführen zu können:

**Schritt 1**: Die Jar-Datei pdfmergev1.jar in ein leeres Verzeichnis kopieren.

**Schritt 2**: Anlegen der Umgebungsvariablen **PATH_TO_FX**, die als Wert den Verzeichnispfad des lib-Unterverzeichnis in dem Verzeichnis mit den JavaFX-Dateien erhält (darum kümmert sich das PowerShell-Skript).

**Schritt 3**: Start der Jar-Datei über den Java Launcher

java --module-path $PATH_TO_FX --add-modules javafx.controls -jar pdfmergev1.jar

Dann sollte das Fenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann hoffentlich selbsterklärend.

**Hinweis:**

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet. Die Datei app.config muss im Benutzerprofil-Verzeichnis angelegt werden.

Ihr Inhalt ist sehr einfach:

xJustizPfad=C:\\EurekaFach\\BEAkten

Über die Datei Log4j2.xml kann der Logger konfiguriert werden (noch nicht getestet, da es anscheinend auch ohne die Xml_Datei funktioniert).

#Versionsgeschichte:

* 0.16 - erste Version mit Minimalfunktionsumfang
* 0.20 - XML-Schemavalidierung mit Logging-Meldung
* 0.22 - keine neuen Funktionen, aber Aktualisierung auf Log4J 2.17.1 (aus aktuellem Anlass)
* 0.24 - Abfrage auf app.config nach dem Start und "Umstellen" auf Log4J 2.17.1
* 0.32 - Mehr Details in der Baumansicht und korrekte Bookmarks für die Gesamt-PdfDatei
* 0.33 - Aktuelle Version mit korrekter Anzeige der Log4j-Versionsnummer nach dem Start in der Log-Datei
