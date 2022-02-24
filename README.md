# xpdfmerge
 /usr/lib/jvm/javafx-sdk-17.0.2/lib
*xpdfmerge* ist eine kleine Java-Anwendung, die alle über Verzeichnispfade in einer Xml-Datei im XJustiz-Format enthaltenen Pdf-Datei zu einer einzigen Pdf-Datei zusammengefasst.

Der Autor der Anwendung ist Mitglied des EurekaFach-Entwicklerteams.

Die Anwendung kann unter Windows, MacOS und Linux ausgeführt werden.

Voraussetzung für die Ausführung der Anwendung ist, dass das JDK (ab Version 11) installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Theoretisch ist das Liberica SDK eine gute Alternative (https://bell-sw.com/pages/downloads/), da hier (bei Full SDK) JavaFx dabei ist - das habe ich aber noch nicht getestet. Auf der anderen Seite ist es eventuell flexibler, die JavaFx-Dateien beim Start der Jar-Datei auswählen zu können. Aktuell geht die Start-Datei davon aus, dass alle JavaFx-Dateien in einem eigenen Verzeichnis vorliegen.

Da (bekanntlich) JavaFx ab JDK 9 nicht mehr dabei ist, müssen die Progrmambibliotheken separat heruntergeladen und in einem eigenen Verzeichnis abgelegt werden. Dieser Verzeichnispfad muss über den Parameter **--module-path** bei der Ausführung der Jar-Datei angegeben werden (mehr dazu später).

Die offizielle Downloadadresse der JavaFx-Dateien ist:

https://openjfx.io/ 

Andere Links sollten nicht genutzt werden (es sei denn, es explizit klar was sich dahinter "verbirgt").

**Hinweis:** Hinter *JavaFX* steht die Firma *Gluon*, so dass man bei der Suche nach dem Stichwort JavaFx ab und zu direkt auf der Firmenwebseite (https://gluonhq.com/) landen kann.

Wichtig ist allgemein, dass sich die Programmdatei Java, z.B. in der Konsole, ausführen lässt. Damit können generell Java-Programme ausgeführt werden und die Umgebungsvariable *JAVA_HOME* enthält den richtigen Verzeichnispfad.

Wenn nicht, muss die Umgebungsvariable _JAVA_HOME_ angelegt und auf den Pfad des JDK gesetzt werden, z.B. bei Windows *C:\Program Files\RedHat\java-11-openjdk-11.0.9-3*

Aufgrund der erwähnten Abhängigkeit des Programms zu JavaFx, muss der Verzeichnispfad der JavaFx-Lib-Dateien beim Aufruf von *java* angegeben werden. Sollten diese Dateien Teil eines Gesamtpakets sein, kommt es darauf an, dass das richtige Unterverzeichnis angegeben wird.

Damit dieser Pfad beim Start des Java-Launchers nicht jedes Mal angegeben werden muss, habe ich eine kleine PowerShell-Skriptdatei angelegt - diese ist aber nur eine Option. Ein Bash-Skript kommt natürlich genauso in Frage.

Es müssen zwei Schritte durchgeführt werden:

1) Setzen der Umgebungsvariablen *PATH_TO_FX* auf das lib-Unterverzeichnis im JavaFx-Verzeichnis

2) Starten von java mit den erforderlichen Angaben.

Die Programmdatei besteht (wie bei Java üblich) aus einer einzigen Jar-Datei.

Sie ist Teil dieses Projekts und ist die einzige Datei, die benötigt wird:

https://github.com/pemo11/xpdfmerge/blob/main/deploy/pdfmergev1.jar

**Wichtig:** Das Java (11) SDK und die JavaFX-Dateien müssen separat installiert werden (am besten natürlich vor dem ersten Programmstart;).

Das kleine (und optionale) PowerShell-Skript ist ebenfalls Teil dieses Projektportals (und wird am einfachsten in der Raw-Ansicht kopiert und in eine lokale Textdatei eingefügt, die als Ps1-Datei lokal gespeichert wird).

Bei den JavaFX-Dateien kommt es (natürlich) darauf an, das sie zum OS passen müssen (Windows, MacOS und Linux).

Die folgende Anleitung gilt allgemein - für Linux gibt es alternativ eine Deb-Datei (mehr dazu am Ende, für MacOS steht auf Anfrage eine Dmg-Datei zur Verfügung).

Die einzelnen Schritte, um XpdfMerge ausführen zu können:

**Schritt 1**: Die Jar-Datei pdfmergev1.jar wird in ein leeres Verzeichnis kopiert.

**Schritt 2**: Es wird eine Umgebungsvariable **PATH_TO_FX** angelegt, die als Wert den Verzeichnispfad des lib-Unterverzeichnis in dem Verzeichnis mit den JavaFX-Dateien erhält (darum kümmert sich das kleine PowerShell-Skript).

**Schritt 3**: Start der Jar-Datei über den Java Launcher

java --module-path $PATH_TO_FX --add-modules javafx.controls -jar pdfmergev1.jar

Dann sollte das Anwendungsfenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann hoffentlich selbsterklärend.

**Hinweis:**

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet. Die Datei app.config muss im Benutzerprofil-Verzeichnis angelegt werden.

Ihr Inhalt ist sehr einfach:

xJustizPfad=C:\\EurekaFach\\BEAkten

Über die Datei Log4j2.xml kann der Logger konfiguriert werden (noch nicht getestet, da es anscheinend auch ohne die Xml_Datei funktioniert).

#Installation unter Linux

Die obige "Installationsanleitung" funktioniert natürlich auch unter Linux. Damit die Anwendung etwas komfortabler per Doppelklick gestartet werden kann, gibt es eine Alternative in Gestalt einer Deb-Datei und einer desktop-Datei, die das Skript *start.sh* ausführt.

Aktuell müssen daher noch mehrere Schritte ausgeführt werden, um eine Datei zu erhalten, die "doppelgeklickt" werden kann:

**Schritt 1:** Download der Deb-Datei aus dem deploy-Verzeichnis im Projektportal

**Schritt 2:** Für die Installation einer Deb-Datei habe ich unter Ubuntu mit **GDebi** gute Erfahrungen gemacht (es muss per *sudo apt-get gdebi* installiert werden). Ansonsten geht es auch mit der eingebauten Anwendung.

**Schritt 3:** Alle Dateien werden in das Verzeichnis */usr/local/bin* kopiert (das wird ggf. noch geändert).

**Schritt 4:** In der Datei *start.sh* muss das Verzeichnis der JavaFx-Dateien angepasst werden (voreingestellt ist */usr/lib/jvm/javafx-sdk-17.0.2/lib*).

**Schritt 5:** Die Datei *pdfmergev1.desktop* muss über *Properties* im Kontextmenü ausführbar gemacht werden.

**Schritt 6:** Optional kann die Datei *pdfmergev1.desktop* in das desktop-Verzeichnis kopiert werden, so dass sie einfacher erreichbar ist.

Danach sollte das Anwendungfenster starten.

#Versionsgeschichte:

* 0.16 - erste Version mit Minimalfunktionsumfang
* 0.20 - XML-Schemavalidierung mit Logging-Meldung
* 0.22 - keine neuen Funktionen, aber Aktualisierung auf Log4J 2.17.1 (aus aktuellem Anlass)
* 0.24 - Abfrage auf app.config nach dem Start und "Umstellen" auf Log4J 2.17.1
* 0.32 - Mehr Details in der Baumansicht und korrekte Bookmarks für die Gesamt-PdfDatei
* 0.33 - Aktuelle Version mit korrekter Anzeige der Log4j-Versionsnummer nach dem Start in der Log-Datei
