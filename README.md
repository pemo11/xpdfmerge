# xpdfmerge

(letzte Aktualisierung: 08/03/2022)

*xpdfmerge* ist eine kleine Java-Anwendung, die alle in einer XJustiz-Nachricht über Dateinamen angegebenen Pdf-Dateien zu einer Pdf-Datei zusammenfasst und diese im documents-Verzeichnis ablegt.

Der Autor der Anwendung ist Mitglied des EurekaFach-Entwicklerteams.

Die Anwendung kann unter Windows, MacOS und Linux ausgeführt werden.

Die Anwendung wird auf zwei unterschiedlichen Wegen zur Verfügung gestellt:

1. Als Quelltext über dieses Projektportal.
2. Als lauffähige Packages für MacOS und Linux über das Ftp-Verzeichnis (der Zugang muss über die unten angegebene E-Mail-Adresse angefordert werden).

Hinweise zur Ausführung unter MacOS und Linux folgen am Ende dieser Übersicht.

Für die Ausführung unter Windows stehen aktuell *nur* die Quelltextdateien im Rahmen eines Maven-Projekts zur Verfügung, das in Eclipse, IntelliJ, Visual Code usw. erstellt werden muss. Es gibt daher keinen Installer (die jar-Datei im deploy-Verzeichnis ist nicht automatisch auf dem aktuellen Stand).

Voraussetzung für die Ausführung der Anwendung ist, dass das JDK (ab Version 11) installiert ist, z.B. über das Open JDK von RedHat:

https://developers.redhat.com/products/openjdk/download

Theoretisch ist das Liberica SDK eine gute Alternative (https://bell-sw.com/pages/downloads/), da hier (bei Full SDK) JavaFx dabei ist - das habe ich aber noch nicht getestet. Auf der anderen Seite ist es eventuell flexibler, die JavaFx-Dateien beim Start der Jar-Datei über den *--module-path*-Parameter auswählen zu können. Aktuell geht die Start-Datei davon aus, dass alle JavaFx-Dateien in einem eigenen Verzeichnis vorliegen (das bei der Linux-Version Teil der deb-Datei ist, die über das EurekaFach-Ftp-Verzeichnis zur Verfügung gestellt wird).

Da (bekanntlich) JavaFx ab JDK 9 nicht mehr dabei ist, müssen die Programmbibliotheken separat heruntergeladen und in einem eigenen Verzeichnis abgelegt werden. Dieser Verzeichnispfad muss über den Parameter **--module-path** bei der Ausführung der Jar-Datei angegeben werden (mehr dazu später).

Die offizielle Downloadadresse der JavaFx-Dateien ist:

https://openjfx.io/ 

Andere Links sollten nicht genutzt werden (es sei denn, es ist explizit klar, was sich dahinter "verbirgt").

**Hinweis:** Hinter *JavaFX* steht die Firma *Gluon*, so dass man bei der Suche nach dem Stichwort JavaFx ab und zu direkt auf der Firmenwebseite (https://gluonhq.com/) landen kann.

Wichtig ist allgemein, dass sich die Programmdatei java, z.B. in der Konsole, ausführen lässt. Damit können generell Java-Programme ausgeführt werden und die Umgebungsvariable *JAVA_HOME* enthält den richtigen Verzeichnispfad. Per

`java -version`

lässt sich bekanntlich feststellen, ob es eine Version > 1.8 ist.

Sollte *java* nicht aufrufbar sein, obwohl ein Java SDK installiert wurde, muss die Umgebungsvariable _JAVA_HOME_ eventuell noch angelegt und auf den Pfad des JDK gesetzt werden, z.B. bei Windows *C:\Program Files\RedHat\java-11-openjdk-11.0.9-3*

Aufgrund der erwähnten Abhängigkeit des Programms zu JavaFx, muss der Verzeichnispfad der JavaFx-Lib-Dateien beim Aufruf von *java* angegeben werden. Sollten diese Dateien Teil eines Gesamtpakets sein, kommt es darauf an, dass das richtige Unterverzeichnis angegeben wird.

Damit dieser Pfad beim Start des Java-Launchers nicht jedes Mal angegeben werden muss, habe ich eine kleine PowerShell-Skriptdatei angelegt - diese ist aber nur eine Option. Ein Bash-Skript kommt natürlich genauso in Frage.

Es müssen zwei Schritte durchgeführt werden:

1) Setzen der Umgebungsvariablen *PATH_TO_FX* auf das lib-Unterverzeichnis im JavaFx-Verzeichnis

2) Starten von java mit den erforderlichen Angaben (ein Beispiel folgt etwas weiter unten auf dieser Seite).

Die Programmdatei besteht (wie bei Java üblich) aus einer einzigen Jar-Datei.

Sie ist Teil dieses Projekts und ist die einzige Datei, die benötigt wird:

https://github.com/pemo11/xpdfmerge/blob/main/deploy/pdfmergev1.jar

**Wichtig:** Das Java (11) SDK und die JavaFX-Dateien müssen bei der Ausführung unter Windows separat installiert werden (am besten natürlich vor dem ersten Programmstart;).Für MacOS ist alles was zur Ausführung erforderlich ist Teil der dmg-Datei, für Linux enthält die deb-Datei aktuell nur die JavaFx-Dateien.

Das kleine (und optionale) PowerShell-Skript ist ebenfalls Teil dieses Projektportals (und wird am einfachsten in der Raw-Ansicht kopiert und in eine lokale Textdatei eingefügt, die als Ps1-Datei lokal gespeichert wird).

Bei den JavaFx-Dateien kommt es (natürlich) darauf an, dass sie zum OS passen müssen (Windows, MacOS und Linux).

Die folgende Anleitung gilt allgemein (für MacOS und Linux sollte man die Package-Dateien aus dem Ftp-Verzeichnis verwenden).

Die einzelnen Schritte, um XpdfMerge ausführen zu können:

**Schritt 1**: Die Jar-Datei pdfmergev1.jar wird in ein leeres Verzeichnis kopiert.

**Schritt 2**: Es wird eine Umgebungsvariable **PATH_TO_FX** angelegt, die als Wert den Verzeichnispfad des lib-Unterverzeichnis in dem Verzeichnis mit den JavaFx-Dateien erhält (darum kümmert sich das kleine PowerShell-Skript).

**Schritt 3**: Start der Jar-Datei über den Java Launcher

java --module-path $PATH_TO_FX --add-modules javafx.controls -jar pdfmergev1.jar

Dann sollte das Anwendungsfenster angezeigt werden und es sollten ein paar Meldungen in der Konsole erscheinen.

Der Rest ist dann hoffentlich selbsterklärend.

**Hinweis:**

Die app.config-Datei enthält den Pfad, in dem sich die Nachrichten-Dateien befinden für den Auswahldialog als Voreinstellung. Gibt es die Datei nicht, wird eine allgemeine Voreinstellung verwendet. Die Datei app.config muss im Benutzerprofil-Verzeichnis angelegt werden.

Ihr Inhalt ist sehr einfach:

xJustizPfad=C:\\EurekaFach\\BEAkten

Außerdem kann in dieser Datei die Schemavalidierung für die XJustiz-Nachricht aktiviert werden (mehr dazu am Ende).

Über die Datei Log4j2.xml kann der Logger konfiguriert werden (u.a. den Pfad der Log-Datei).

Installation unter Linux
========================

Die obige "Installationsanleitung" funktioniert natürlich auch unter Linux. Damit die Anwendung etwas komfortabler per Doppelklick gestartet werden kann, gibt es eine Alternative in Gestalt einer deb-Datei, die sich im Ftp-Verzeichnis befindet.

*Wichtig*: Aktuell enthält die deb-Datei nur die JavaFx-Dateien - das Java 11 SDK muss vorher instaliert werden. JavaFx muss daher *nicht* installiert werden.

Die deb-Datei enthält eine desktop-Datei, die das Skript *start.sh* ausführt, das wiederum den Java-Launcher *java* wie gezeigt startet.

Aktuell müssen drei Schritte ausgeführt werden, um eine Datei zu erhalten, die auf einem Linux-Desktop (Debian) "doppelgeklickt" werden kann:

**Schritt 1:** Download der deb-Datei aus dem Ftp-Verzeichnis.

**Schritt 2:** Für die Installation einer Deb-Datei habe ich mit **GDebi** gute Erfahrungen gemacht (es muss eventuell per *sudo apt-get gdebi* installiert werden). Ansonsten geht es auch mit dem eingebauten Package Installer.

**Wichtig**: Aktuell gibt es mit der deb-Datei leider ein Problem, das dazu führt, das die Installation nicht ausgeführt wird (es hängt mit dem Umstand zusammen, dass aus einem mir nicht bekannten Grund Dateien, die Teil des Package sind nicht überschrieben werden können).

Eine provisorische Lösung besteht darin, die deb-Datei direkt im Terminal-Fenster zu installieren:

`sudo dpkg -i --force-overwrite EFXPdfMerge.deb`

**Schritt 3:** Alle Dateien werden durch die Installation in das Verzeichnis */usr/local/bin* kopiert (das wird ggf. noch geändert).

Optional kann die Datei *pdfmergev1.desktop* in das desktop-Verzeichnis kopiert werden, so dass sie einfacher erreichbar ist.

Über

`sudo desktop-file-install pfdmergev1.desktop`

wird die desktop-Datei im Verzeichnis `/usr/share/applications` abgelegt und kann dort per Doppeklick gestartet werden.

Danach sollte das Anwendungfenster starten.

In der aktuellen Version der deb-Datei erledigt das postinst-Skript den letzten Schritt, so der Eintrag automatisch bei den "geteilten Anwendungen" erscheinen sollte. Bei Ubuntu erscheint er aber nicht in der Liste der installierten Anwendungen.

Bei Fragen und Problemen bitte (formlos) eine Mail an **pdfmerger@eureka-fach.de**

Schemavalidierung
=================
Über die (optionale) Konfigurationsdatei *efxapp.config* kann eine Schemavalidierung für die XJustiz-Nachricht aktiviert werden. Danach werden Schemafehler angezeigt bzw. in der Logdatei vermerkt, die Nachrichtendatei wird aber immer geladen. Dadurch lässt sich feststellen, ob der Absender valide Nachrichten in Bezug auf die aktuelle XJustiz-Version verschickt.

Die Einträge sind:
- schemaValidierung=ein
- schemaVersion=3.3.1
- schemaPfad=xjustiz-schemas/xjustiz_0005_nachrichten_3_0.xsd

Über *schemaValidierung=ein* wird die Schemavalidierung aktiviert (jeder andere Begriff oder kein Eintrag deaktiviert die Validierung). Dann muss *schemaPfad* auf die Xsd-Datei verweisen, die eine Nachricht validiert (die Schemadateien gibt es auf der XJustiz-Webseite bzw. im *usr/bin/local*-Verzeichnis im Verzeichnis *schemas*, das durch die deb-Datei angelegt wird).

**Wichtig**: Der Schemapfad ist immer relativ zum *home*-Verzeichnis. Hier darf also kein absoluter Pfad stehen.

Die *schemaVersion* hat lediglich Infozwecke, was hier steht spielt daher keine Rolle.

#Versionsgeschichte:

* 0.16 - erste Version mit Minimalfunktionsumfang
* 0.20 - XML-Schemavalidierung mit Logging-Meldung
* 0.22 - keine neuen Funktionen, aber Aktualisierung auf Log4J 2.17.1 (aus aktuellem Anlass)
* 0.24 - Abfrage auf app.config nach dem Start und "Umstellen" auf Log4J 2.17.1
* 0.32 - Mehr Details in der Baumansicht und korrekte Bookmarks für die Gesamt-PdfDatei
* 0.33 - Korrekte Anzeige der Log4j-Versionsnummer nach dem Start in der Log-Datei
* 0.35 - u.a. Anzeige von Validierungsfehlern, Anwendungsicon und nummerierte Dokument-Einträge in der Baumansicht
* 0.36 - Ein PdfMerge ist nur möglich, wenn eine Xml-Datei geladen wurde
* 0.37 - Detailierte Log-Einträge zur Schemavalidierung
* 0.39 - Konfiguration der Schemavalidierung über die efxapp.config
