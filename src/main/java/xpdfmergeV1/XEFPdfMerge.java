/*
  XJustiz-Pdf-Merge für Windows, MacOs und Linux
  Autor: Peter Monadjemi - pdfmerge@eureka-fach.de
  Letzte Änderung: 28/05/25
*/

package xpdfmergeV1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Wird nur über das Öffnen einer Pdf-Datei benötigt
import java.awt.Desktop;

public class XEFPdfMerge extends Application {
    private String osName = "Unbekannt";
    private static final String appVersion = "0.46";
    // Nur provisorisch - falls die Version-Abfrage null liefert
    private String log4VersionDefault = "2.17.1";
    // Kein Scope Modifier, daher Sichtbarkeit innerhalb des Package
    static  Logger logger = null; // LogManager.getLogger(XEFPdfMerge.class);
    private XmlHelper xmlHelper = null;
    private String xmlPfad = "";
    private String basePfad = "";
    private String pdfPfad = "";
    private String infoMessage = "";
    private String xJustizPfad = "";
    private String pdfOutfile = "GesamtePDF.pdf";
    private String userHome = "";
    private String imgPfad = "";
    private int dokumentNr = 0;
    // Betrifft die XML-Validierung
    private boolean errorFlag = false;
    // Wichtig: LinkedHashMap statt (veralteter) Hashtable, da die Reihenfolge erhalten bleibt
    private LinkedHashMap<AkteInfo, List<PdfDocumentInfo>> pdfInfoHashtable = null;
    // LRU-Liste
    private List<String> lruList = null;
    private int lruCounter = 0;
    private MenuItem[] lruItems = null;
    private final int MAX_LRU_ENTRIES = 4;
    private AppConfig config = null;
    // Menuitem-Elemente
    private MenuItem exitItem = null;
    private MenuItem openNachrichtXmlItem = null;
    private MenuItem pdfMergeItem = null;
    private MenuItem lruListItem = null;

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        // TODO: Unter Linux sollte die Log-Datei unter ~/.local/share/applications abgelegt werden
        // Geht eventuell per PropertyConfigurator.configure(...)
        logger = LogManager.getLogger(XEFPdfMerge.class);

        // Die Versionsnummer von Log4J loggen
        String log4JVersion = logger.getClass().getPackage().getSpecificationVersion();
        // Nur prosivorisch, da unter Linux null resultiert?
        log4JVersion = log4JVersion == null ? log4VersionDefault : log4JVersion;
        // geht nicht, da versionInfo private ist?
        // String log4JVersion = logger.getClass().getPackage().versionInfo.implVersion;

        // Application Icon festlegen
        try {
            java.net.URL logoUrl = getClass().getResource("/images/EFXLogo.png");
            if (logoUrl != null) {
                imgPfad = logoUrl.toURI().toString();
                Image imgLogo = new Image(imgPfad);
                stage.getIcons().add(imgLogo);
            } else {
                logger.warn("Logo image resource /images/EFXLogo.png not found in classpath");
                // Try alternate locations
                File logoFile = new File("images/EFXLogo_512x512.png");
                if (logoFile.exists()) {
                    imgPfad = logoFile.toURI().toString();
                    Image imgLogo = new Image(imgPfad);
                    stage.getIcons().add(imgLogo);
                    logger.info("Loaded logo from alternate location: " + imgPfad);
                } else {
                    logger.error("Could not find any logo image file");
                }
            }
        } catch (Exception ex) {
            logger.error("Error loading application icon: " + ex.getMessage(), ex);
        }

        // User directory holen
        userHome = System.getProperty("user.home");

        // OS-Name holen
        osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        infoMessage = String.format("XEFPdfMerge->start: version=%s osName=%s", appVersion, osName);
        logger.info(infoMessage);

        infoMessage = String.format("*** Using Log4J version %s ***", log4JVersion);
        logger.info(infoMessage);

        // Variablen initalisieren
        // lruList = new LRUList(4);
        lruList = new ArrayList<>(MAX_LRU_ENTRIES);

        // Ausgabeverzeichnis OS-spezifisch festlegen
        // Ist u.U. nicht erforderlich, da user.home bereits OS-spezifisch ist
        if (osName.contains("win")) {
            pdfOutfile = Paths.get(userHome, "documents", pdfOutfile).toString();
        } else if (osName.contains("mac")) {
            pdfOutfile = Paths.get(userHome, "documents", pdfOutfile).toString();
        } else {
            // Problem bei Linux-Desktop: Documents statt documents und documents kann auch Dokumente heißen
            pdfOutfile = Paths.get(userHome,  pdfOutfile).toString();
        }
        infoMessage = String.format("Home-Directory=%s,pdfOutfile=%s", userHome, pdfOutfile);
        logger.info(infoMessage);

        // xJustiz-Pfad aus Config-Datei einlesen

        try {
            config = new AppConfig();
            xJustizPfad = config.getProperty("xJustizPfad");
        } catch (Exception ex) {
            infoMessage = "Config-Datei kann nicht gelesen werden und wird ausgelassen.";
            XEFPdfMerge.logger.error(infoMessage);
        }

        // Wenn leer, dann Default setzen
        if (xJustizPfad == null || xJustizPfad.isEmpty()) {
            xJustizPfad = Paths.get(userHome, "documents").toString();
        }

        infoMessage = String.format("XEFPdfMerge->start: xJustizPfad=%s", xJustizPfad);
        logger.info(infoMessage);

        // FXMLLoader fxmlLoader = new FXMLLoader(XEFPdfMerge.class.getResource("mainView.fxml"));

        // Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        // StandardController controller = fxmlLoader.getController();
        // System.out.println(controller);

        // controller.setStage(stage);

        // Menu hinzufügen
        MenuBar menuBar = new MenuBar();

        VBox vbox1 = new VBox(menuBar);

        Scene scene = new Scene(vbox1, 800,600);

        final ProgressBar progressBar = new ProgressBar(0);
        // progressBar.setVisible(false);

        // Breite der Progressbar = Breite des Containers
        progressBar.setMaxWidth(scene.getWidth());

        // Höhe kann nicht gesetzt werden?
        // progressBar.setMaxHeight(Double.MAX_VALUE);

        // final ProgressIndicator progressIndicator = new ProgressIndicator();

        // vbox.getChildren().addAll(progressBar);
        VBox vbox2 = new VBox();

        Label lbl1 = new Label();
        lbl1.setText("Dokumente");

        Label lbl2 = new Label();
        lbl2.setText("Status");

        Label lbl3 = new Label();
        lbl3.setText("OK");
        // Vordergrundfarbe muss per CSS gesetzt werden
        lbl3.setStyle("-fx-text-fill:white;");
        lbl3.setBackground(new Background(new BackgroundFill(Color.BLACK,null,null)));

        // hbox für TreeView anlegen
        // HBox hBox = new HBox();

        // hBox.setPadding(new Insets(50, 5 , 5, 50));
        vbox2.setPadding(new Insets(10, 5 , 5, 10));

        // TreeView anlegen
        TreeView trvAkten = new TreeView();

        // Eventhandler für DoubleClick auf ein TreeItem anlegen
        trvAkten.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    // Alert alert = new Alert(Alert.AlertType.INFORMATION, infoMessage, ButtonType.FINISH);
                    // alert.setTitle("DoubleClick-Event");
                    // TreeItem<Dokument> selectedItem = (TreeItem<Dokument>) trvAkten.getSelectionModel().getSelectedItem();
                    TreeItem<String> selectedItem = (TreeItem<String>)trvAkten.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        // alert.setHeaderText(selectedItem.getValue().getDateiname());
                        try {
                            String treeItemText = selectedItem.getValue();
                            String pdfDateiname = "";
                            String filePattern = "Dokument\\s+\\((\\d+)\\)\\s+(.+\\.pdf)";
                            Pattern pattern = Pattern.compile(filePattern);

                            Matcher m = pattern.matcher(treeItemText);
                            if (m.find()) {
                                pdfDateiname = m.group(2);
                            } else {
                                filePattern = "Datei=(.*\\.pdf)";
                                pattern = Pattern.compile(filePattern);
                                m = pattern.matcher(treeItemText);
                                if (m.find()) {
                                    pdfDateiname = m.group(1);
                                }
                            }
                            if (pdfDateiname != "") {
                                // alert.setHeaderText(pdfDateiname);
                                // alert.showAndWait();
                                infoMessage = "Doppelklick mit Datei=" + pdfDateiname;
                                logger.info(infoMessage);

                                pdfPfad = basePfad +"/" + pdfDateiname;
                                File file = new File(pdfPfad);
                                if (file.exists()) {
                                    // support for Desktop?
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop desktop = Desktop.getDesktop();
                                        try {
                                            desktop.open(file);
                                        } catch (IOException ex) {
                                            infoMessage = "Fehler im Eventhandler setOnMouseClicked (" + ex.getMessage() + ")";
                                            logger.info(infoMessage);
                                        }
                                    } else {
                                        infoMessage = "Keine Desktop-Unterstützung - kein Doppelklicker möglich!";
                                        logger.info(infoMessage);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            infoMessage = "Fehler im Event-Handler setOnMouseClicked (" + ex.getMessage() + ")";
                            logger.info(infoMessage);
                        }
                    }
                }
            }
        });

        // ???
        trvAkten.setPadding(new Insets(10, 20, 10, 20));

        // TreeView soll in der vbox "wachsen"
        vbox2.setVgrow(trvAkten, Priority.ALWAYS);

        // TreeView zur HBox hinzufügen
        // hBox.getChildren().add(trvDokumente);

        vbox2.getChildren().addAll(lbl1, trvAkten, lbl2, lbl3);

        // Weitere hbox für TreeView und ImageView
        HBox hbox1 = new HBox();

        /*

        ImageView imageView = new ImageView();

        InputStream inputStream = new FileInputStream("images/pdfDefaultImage.png");
        Image image = new Image(inputStream);
        imageView.setImage(image);
        // Was bewirken diese Aufrufe?
        imageView.setX(10);
        imageView.setY(10);
        imageView.setFitWidth(320);
        imageView.setPreserveRatio(true);

        hbox1.getChildren().addAll(vbox2, imageView);
        */

        // vbox1.getChildren().add(vbox2);
        vbox1.getChildren().add(vbox2);

        Menu menuFile = new Menu("eAkte");

        // PM: 04/02/22 - image Pfad als uri holen - eventuell try/catch statt Methoden-Erweiterung
        try {
            java.net.URL imageUrl = getClass().getResource("/images/nachrichtxml.png");
            if (imageUrl != null) {
                imgPfad = imageUrl.toURI().toString();
                openNachrichtXmlItem = new MenuItem("Nachricht.xml öffnen", new ImageView(new Image(imgPfad)));
            } else {
                logger.warn("nachrichtxml.png resource not found in classpath");
                openNachrichtXmlItem = new MenuItem("Nachricht.xml öffnen");
            }
        } catch (Exception ex) {
            logger.error("Error loading nachrichtxml.png: " + ex.getMessage(), ex);
            openNachrichtXmlItem = new MenuItem("Nachricht.xml öffnen");
        }

        /**
        * Xml-Nachricht einlesen und in treeView darstellen
         */
        openNachrichtXmlItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                AkteInfo akteInfo = null;
                FileChooser fileChooser = new FileChooser();
                // Gibt es einen gespeicherten Pfad?
                if (config != null && config.getProperty("LastPathSelected") != null) {
                    String lastPath = config.getProperty("LastPathSelected");
                    File lastpathFile = new File(lastPath);
                    // Gibt es den zuletzt verwendeten Pfad noch?
                    if (lastpathFile.exists()) {
                        fileChooser.setInitialDirectory(new File(lastPath));
                    }
                }

                // fileChooser.setInitialDirectory(new File(xJustizPfad));
                fileChooser.setTitle("Auswahl Nachricht.xml");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Xml-Dateien", "*.xml"),
                        new FileChooser.ExtensionFilter("Alle Dateien", "*.*")
                );

                File selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {

                    // Menuitem Gesamt-Pdf erstellen aktivieren
                    pdfMergeItem.setDisable(false);
                    
                    // Ensure the icon is preserved
                    if (pdfMergeItem.getGraphic() == null) {
                        try {
                            String iconPath = getClass().getResource("/images/pdfmerge.png").toURI().toString();
                            pdfMergeItem.setGraphic(new ImageView(new Image(iconPath)));
                        } catch (Exception ex) {
                            infoMessage = String.format("Icon für GesamtPDF erstellen konnte nicht geladen werden: %s", ex.getMessage());
                            logger.warn(infoMessage);
                        }
                    }

                    // Klassenvariable wird ohne this angesprochen
                    xmlPfad = selectedFile.toString();
                    infoMessage = String.format("xmlPfad = %s", xmlPfad);
                    logger.info(infoMessage);

                    // Basispfad festlegen
                    basePfad = selectedFile.getParent();

                    // Basispfad in Config-Datei speichern (sofern vorhanden)
                    if (config != null) {
                        config.setProperty("LastPathSelected", basePfad);
                    }
                    
                    // Pfad zur LRU-Liste hinzufügen, wenn er noch nicht enthalten ist
                    // PM: Benennung der Variablen nicht optimal
                    pdfPfad = basePfad + "/" + xmlPfad;
                    if (!lruList.contains(xmlPfad)) {
                        // Wenn die Liste bereits MAX_LRU_ENTRIES Einträge hat, ältesten Eintrag entfernen
                        if (lruList.size() >= MAX_LRU_ENTRIES) {
                            lruList.remove(0); // Ältesten Eintrag entfernen (am Anfang der Liste)
                        }
                        // Neuen Eintrag am Ende hinzufügen
                        lruList.add(xmlPfad);
                        infoMessage = String.format("%s zur LRU-Liste hinzugefügt", xmlPfad);
                        logger.info(infoMessage);
                    } else {
                        // Wenn der Eintrag bereits existiert, an das Ende der Liste verschieben (am häufigsten verwendet)
                        lruList.remove(xmlPfad);
                        lruList.add(xmlPfad);
                        infoMessage = String.format("%s in der LRU-Liste aktualisiert", xmlPfad);
                        logger.info(infoMessage);
                    }
                    
                    // Menüeinträge aktualisieren
                    updateLruMenuItems();
                    
                    String anzeigeName = "";
                    ;
                    String dateiName = "";
                    String zeitpunktErstellung = "";

                    // dokumentNr für die Dokumenteliste in der TreeView zurücksetzen
                    dokumentNr = 0;
                    // Fehlerflag zurücksetzen
                    errorFlag = false;

                    // Anlegen der Bookmarks in der Ausgabe-Pdf
                    // Pro Dokument soll eine Bookmark angelegt werden - allerdings hierarchisch Akte -> Dokument1 -> Dokument2 usw.
                    // Der Key ist ein AkteInfo-Objekt mit Namen der Akte, der Value eine Liste mit PdfInfo-Objekten, die für alle
                    // Pdf-Dateien der Akte stehen
                    pdfInfoHashtable = new LinkedHashMap<>();

                    boolean errorFlag = false;
                    try {
                        // Xml-Datei laden (ohne Validierung)
                        XmlHelper xmlHelper = new XmlHelper(logger, xmlPfad);
                        // Eine Schema-Validierung wird nur durchgeführt, wenn die Config-Datei den Eintrag schemaValidierung = ein enthält
                        // Der Eintrag schemaPfad gibt das Verzeichnis mit den schema-Dateien an
                        // Das erscheint mir als die einfachste Lösung
                        // String xsdPfad = "/schemas/xjustiz_0005_nachrichten_3_0.xsd";
                        if (config != null) {
                            String schemaValidierung = config.getProperty("schemaValidierung");
                            // 18/09/22 - Validierung gegen die neuste XJustiz-Version testen
                            // ??? Die neuste Version ist aber 3.3.1 - die Versionsnummernabfrage ist daher eigentlich falsch?
                            // bzw. allgemein mit einem Dokument nach dem neuen Standard testen
                            String schemaVersion = "3.3.1";
                            String schemaPfad = "";
                            if (schemaValidierung != null && schemaValidierung.toLowerCase().equals("ein")) {
                                if (config.getProperty("schemaVersion") != "") {
                                    schemaVersion = config.getProperty("schemaVersion");
                                }
                                schemaPfad = config.getProperty("schemaPfad");
                                if (schemaPfad != null) {
                                    // Xsd-Pfad ist aktuell relativ zum home-Directory
                                    schemaPfad = Paths.get(userHome, schemaPfad).toString();
                                    if (!new File(schemaPfad).exists()) {
                                        infoMessage = String.format("Die Schemadatei %s existiert nicht - Schemavalidierung wurde ausgelassen.", schemaPfad);
                                        logger.warn(infoMessage);
                                    } else {
                                        infoMessage = String.format("Die XJustiz-Nachricht wird gegen %s Version %s validiert.", schemaPfad, schemaVersion);
                                        logger.info(infoMessage);
                                        List<String> validateErrors = xmlHelper.validateXMLSchema(schemaPfad, xmlPfad);
                                        // Gab es Validierungsfehler, alle loggen, die Ausführung geht weiter
                                        if (validateErrors.size() == 0) {
                                            infoMessage = "XSD-Schemavalidierung ohne Fehler";
                                            logger.info(infoMessage);
                                        } else {
                                            String schemaInfo = "XJustiz-Nachricht v" + schemaVersion;
                                            infoMessage = String.format("Schemavalidierung gegen %s mit %d Fehlern", schemaInfo, validateErrors.size());
                                            logger.warn(infoMessage);
                                            for (String validateError : validateErrors) {
                                                logger.warn(validateError);
                                                errorFlag = true;
                                            }
                                            // Fehlermeldung ausgeben
                                            Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.OK);
                                            alert.setTitle("XML-Validierung mit Fehlern");
                                            alert.setHeaderText("");
                                            String contentText = String.format("Beim Laden von %s traten Validierungsfehler auf (XJustiz-Schemaversion %s).", xmlPfad, schemaVersion);
                                            contentText += "\n\n";
                                            contentText += "(weitere Details in der Log-Datei)";
                                            alert.setContentText(contentText);
                                            // Höhe explizit setzen
                                            alert.setHeight(200);
                                            alert.showAndWait();
                                        }
                                    }
                                } else {
                                    infoMessage = "Config-Datei enthält keinen schemaPfad-Eintrag - Schemavalidierung wurde ausgelassen.";
                                    logger.info(infoMessage);
                                }
                            }
                        }

                        // Gab es Validierungsfehler?
                        if (!errorFlag) {
                            // PDF-Dateinamen holen, um Anzahl der Dokumente anzuzeigen
                            List<String> pdfFiles = xmlHelper.getPdfNamen2();
                            int pdfCount = pdfFiles.size();
                            
                            // Erfolgsmeldung ausgeben
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                            alert.setTitle("Hinweis");
                            alert.setHeaderText("");
                            alert.setContentText(xmlPfad + " wurde ausgewertet.\nEnthaltene PDF-Dokumente: " + pdfCount);
                            alert.showAndWait();
                        }
                        // Pfad in "Statusbar" anzeigen
                        lbl3.setText(xmlPfad + " wurde geladen.");

                        PdfHelper pdfHelper = new PdfHelper(logger);
                        List<Akte> akten = xmlHelper.getAkten();
                        TreeItem triRoot = new TreeItem("Akten");
                        // Ins TreeView übertragen
                        for (Akte akte : akten) {
                            String aktenId = akte.getId();
                            anzeigeName = akte.getAnzeigeName();
                            zeitpunktErstellung = akte.getZeitpunktErstellungVersand();
                            TreeItem triAkte = new TreeItem("Akte=" + anzeigeName);
                            triAkte.getChildren().add(new TreeItem("Id=" + aktenId));
                            triAkte.getChildren().add(new TreeItem("Aktentyp=" + akte.getAktenTyp()));
                            triAkte.getChildren().add(new TreeItem("Zeitpunkt Erstellung=" + zeitpunktErstellung));
                            // Alle Teilakten holen
                            List<Teilakte> teilakten = xmlHelper.getTeilakten(aktenId);
                            // Gibt es Teilakten?
                            if (teilakten.size() > 0) {
                                for (Teilakte teilakte : teilakten) {
                                    String teilakteId = teilakte.getId();
                                    anzeigeName = teilakte.getAnzeigeName();
                                    // Akteinfo-Objekt für Teilakte anlegen
                                    akteInfo = new AkteInfo(anzeigeName);
                                    pdfInfoHashtable.put(akteInfo, new ArrayList<>());

                                    TreeItem triTeilakte = new TreeItem("Teilakte=" + anzeigeName);
                                    triTeilakte.getChildren().add(new TreeItem("Id=" + teilakteId));
                                    triTeilakte.getChildren().add(new TreeItem("Nummer im übg. Container=" + teilakte.getNummerImUebergeordnetenContainer()));
                                    // Alle Dokumente durchgehen
                                    List<Dokument> dokumente = xmlHelper.getDokumenteWithXPath(teilakteId, Aktentyp.Teilakte);
                                    for (Dokument dokument : dokumente) {
                                        dokumentNr++;
                                        dateiName = dokument.getDateiname();
                                        TreeItem triDokument = new TreeItem(String.format("Dokument (%d) %s", dokumentNr, dateiName));
                                        triDokument.getChildren().add(new TreeItem("Id=" + dokument.getId()));
                                        pdfPfad = basePfad + "/" + dateiName;
                                        Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                        triDokument.getChildren().add(new TreeItem("Posteingangsdatum=" + dokument.getDatumPosteingang()));
                                        triDokument.getChildren().add(new TreeItem("Veraktungsdatum=" + dokument.getDatumVeraktung()));
                                        triDokument.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                        triTeilakte.getChildren().add(triDokument);
                                        // Eintrag in pdfInfoHashtable, damit das Setzen von Bookmarks später möglich ist
                                        PdfDocumentInfo documentInfo = new PdfDocumentInfo();
                                        documentInfo.setFileName(pdfPfad);
                                        documentInfo.setDisplayName(dokument.getAnzeigename());
                                        documentInfo.setPageCount(pageCount);
                                        documentInfo.getBookmarks().put("Posteingangsdatum", dokument.getDatumPosteingang());
                                        documentInfo.getBookmarks().put("Veraktungsdatum", dokument.getDatumVeraktung());
                                        // Dokument an Dokumenteliste der Akte anhängen
                                        pdfInfoHashtable.get(akteInfo).add(documentInfo);
                                    }
                                    triAkte.getChildren().add(triTeilakte);
                                }
                            } else {
                                // Hashtable-Eintrag für Booksmarks mit dem Aktennamen als Key anlegen
                                akteInfo = new AkteInfo(anzeigeName);
                                pdfInfoHashtable.put(akteInfo, new ArrayList<>());

                                // Alle Dokumente der Akte durchgehen
                                List<Dokument> dokumente = xmlHelper.getDokumenteWithXPath(aktenId, Aktentyp.Akte);
                                for (Dokument dokument : dokumente) {
                                    dokumentNr++;
                                    dateiName = dokument.getDateiname();
                                    anzeigeName = dokument.getAnzeigename();
                                    pdfPfad = basePfad + "/" + dateiName;
                                    Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                    TreeItem triDokument = new TreeItem(String.format("Dokument (%d) %s", dokumentNr, dateiName));
                                    triDokument.getChildren().add(new TreeItem("Id=" + dokument.getId()));
                                    triDokument.getChildren().add(new TreeItem("Datei=" + dokument.getDateiname()));
                                    triDokument.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                    triDokument.getChildren().add(new TreeItem("Posteingangsdatum=" + dokument.getDatumPosteingang()));
                                    triDokument.getChildren().add(new TreeItem("Veraktungsdatum=" + dokument.getDatumVeraktung()));
                                    triAkte.getChildren().add(triDokument);
                                    // Eintrag in pdfInfoHashtable, damit das Setzen von Bookmarks später möglich ist
                                    PdfDocumentInfo documentInfo = new PdfDocumentInfo();
                                    documentInfo.setFileName(pdfPfad);
                                    documentInfo.setDisplayName(dokument.getAnzeigename());
                                    documentInfo.setPageCount(pageCount);
                                    documentInfo.getBookmarks().put("Posteingangsdatum", dokument.getDatumPosteingang());
                                    documentInfo.getBookmarks().put("Veraktungsdatum", dokument.getDatumVeraktung());
                                    // Dokument an Dokumenteliste der Akte anhängen
                                    pdfInfoHashtable.get(akteInfo).add(documentInfo);
                                }
                            }

                            triRoot.getChildren().add(triAkte);

                        }
                        // Root-Element des TreeView hinzufügen
                        trvAkten.setRoot(triRoot);
                    } catch (IOException ex) {
                        infoMessage = String.format("openNachrichtXml-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                        logger.error(infoMessage, ex);
                    } catch (ParserConfigurationException ex) {
                        infoMessage = String.format("openNachrichtXml-ActionHandler: Parser-Fehler (%s)", ex.getMessage());
                        logger.error(infoMessage, ex);
                    } catch (SAXException ex) {
                        infoMessage = String.format("openNachrichtXml-ActionHandler: SAX-Fehler (%s)", ex.getMessage());
                        logger.error(infoMessage, ex);
                    }
                }
            }
        });

        // lruListItem = new MenuItem("Zuletzt geöffnet", new ImageView(new Image(imgPfad)));
        // menuLru.getItems().add(lruListItem);

        Menu menuLru = new Menu("Zuletzt verwendet");
        
        // Menu icons with error handling
        try {
            java.net.URL docListUrl = getClass().getResource("/images/documentlist.png");
            if (docListUrl != null) {
                imgPfad = docListUrl.toURI().toString();
                menuLru.setGraphic(new ImageView(new Image(imgPfad)));
            } else {
                logger.warn("documentlist.png resource not found in classpath");
            }
            
            java.net.URL docUrl = getClass().getResource("/images/document.png");
            if (docUrl != null) {
                imgPfad = docUrl.toURI().toString();
                MenuItem lru1 = new MenuItem("Eintrag 1", new ImageView(new Image(imgPfad)));
                MenuItem lru2 = new MenuItem("Eintrag 2", new ImageView(new Image(imgPfad)));
                MenuItem lru3 = new MenuItem("Eintrag 3", new ImageView(new Image(imgPfad)));
                MenuItem lru4 = new MenuItem("Eintrag 4", new ImageView(new Image(imgPfad)));
                
                lruItems = new MenuItem[]{lru1, lru2, lru3, lru4};
            } else {
                logger.warn("document.png resource not found in classpath");
                MenuItem lru1 = new MenuItem("Eintrag 1");
                MenuItem lru2 = new MenuItem("Eintrag 2");
                MenuItem lru3 = new MenuItem("Eintrag 3");
                MenuItem lru4 = new MenuItem("Eintrag 4");
                
                lruItems = new MenuItem[]{lru1, lru2, lru3, lru4};
            }
            menuLru.getItems().addAll(lruItems);
        } catch (Exception ex) {
            logger.error("Error loading document icons: " + ex.getMessage(), ex);
            MenuItem lru1 = new MenuItem("Eintrag 1");
            MenuItem lru2 = new MenuItem("Eintrag 2");
            MenuItem lru3 = new MenuItem("Eintrag 3");
            MenuItem lru4 = new MenuItem("Eintrag 4");
            
            lruItems = new MenuItem[]{lru1, lru2, lru3, lru4};
            menuLru.getItems().addAll(lruItems);
        }

        menuLru.getItems().forEach((item) -> {
            item.setOnAction(e -> {
                infoMessage = item.getText();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, infoMessage, ButtonType.FINISH);
                alert.setTitle("Test");
                alert.setHeaderText(infoMessage);
                alert.showAndWait();
            });
        });

        menuLru.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                // Alert alert = new Alert(Alert.AlertType.INFORMATION, infoMessage, ButtonType.FINISH);
                // alert.setTitle("Noch nicht implementiert!");
                // alert.setHeaderText("Anzahl LRU-Einträge: " + lruList.size());
                // alert.showAndWait();
                for (int i=0; i < lruList.size(); i++) {
                    // MenuItem für LRU-Item anlegen
                    // MenuItem lruItemNew = new MenuItem(lruItem.toString());
                    // menuLru.getItems().add(lruItemNew);
                    lruItems[i].setText(lruList.get(i));
                }
            }
        });

        // Trennlinie für das Menü
        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        imgPfad = getClass().getResource("/images/exit.png").toURI().toString();
        exitItem =  new MenuItem("Beenden", new ImageView(new Image(imgPfad)));

        exitItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(1);
            }
        });

        menuFile.getItems().addAll(openNachrichtXmlItem, menuLru, sep1, exitItem);

        Menu menuAction = new Menu("Aktionen");
        imgPfad = getClass().getResource("/images/pdfmerge.png").toURI().toString();
        pdfMergeItem = new MenuItem("GesamtPDF erstellen",  new ImageView(new Image(imgPfad)));

        // Menuitem deaktivieren
        pdfMergeItem.setDisable(true);

        // Ausführen der Merge-Aktion
        pdfMergeItem.setOnAction(new EventHandler<ActionEvent>() {
            private Instant startZeit = Instant.now();

            @Override
            public void handle(ActionEvent actionEvent) {

                lbl3.setText("Pdf-Merge wird gestartet.");
                lbl3.requestFocus();
                // Bringt leider nichts - Text wird erst am Ende angezeigt
                // TODO: Auslagern des Merge in einen Task oder einfacher per Platform.runLater()
                // https://riptutorial.com/javafx/example/7291/updating-the-ui-using-platform-runLater

                // Pfade aller Pdf-Dateien aus der Xml-Datei ziehen
                try {
                    xmlHelper = new XmlHelper(logger, xmlPfad);
                } catch (IOException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                } catch (ParserConfigurationException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: Parser-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                } catch (SAXException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: SAX-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                }

                // Basispfad zuweisen
                basePfad = Paths.get(xmlPfad).getParent().toString();

                // Alle Pdf-Dateien durchgehen
                List<InputStream> inputList = new ArrayList<InputStream>();

                List<String> pdfListe = xmlHelper.getPdfNamen2();
                for (String pdfPfad : pdfListe) {
                    String tmpPath = basePfad + "/" + pdfPfad;
                    File pdfFile = new File(tmpPath);
                    if (pdfFile.exists() && pdfFile.isFile()) {
                        // Vorab-Validierung mit PDFBox
                        try (InputStream is = new FileInputStream(pdfFile)) {
                            try {
                                org.apache.pdfbox.pdmodel.PDDocument testDoc = org.apache.pdfbox.Loader.loadPDF(is);
                                testDoc.close();
                                // Wenn kein Fehler: Stream für Merge öffnen
                                inputList.add(new FileInputStream(pdfFile));
                            } catch (Exception pdfEx) {
                                infoMessage = String.format("mergePdf-ActionHandler: Datei %s ist keine gültige PDF oder beschädigt und wird übersprungen. (%s)", tmpPath, pdfEx.getMessage());
                                logger.warn(infoMessage, pdfEx);
                                continue;
                            }
                        } catch (Exception ex) {
                            infoMessage = String.format("mergePdf-ActionHandler: Datei %s konnte nicht geöffnet werden und wird übersprungen. (%s)", tmpPath, ex.getMessage());
                            logger.warn(infoMessage, ex);
                            continue;
                        }
                    } else {
                        infoMessage = String.format("mergePdf-ActionHandler: Datei %s existiert nicht und wird übersprungen.", tmpPath);
                        logger.warn(infoMessage);
                    }
                }

                // Jetzt wird gemerged

                // Die Anzeige eines Fortschritts funktioniert noch nicht, da die Dateien "in einem Rutsch" gemerged werden

                // PdfHelper pdfHelper = new PdfHelper(logger, progressBar);

                // SOllte eigentlich die Progressbar starten, tut es aber noch nicht
                // Ausführung auf einem thread?
                // progressBar.setProgress(-1);
                // progressBar.setVisible(true);
                // vbox1.getChildren().addAll(progressBar);

                PdfHelper pdfHelper = new PdfHelper(logger);

                /*
                Das Setzen der Seitenzahlen sollte nicht mehr erforderlich sein
                Enumeration<String> e = pdfInfoHashtable.keys();
                while (e.hasMoreElements()) {
                    String datei = e.nextElement();
                    PdfInfo pdfInfo = pdfInfoHashtable.get(datei);
                    try {
                        Integer pageCount = pdfHelper.getPdfPageCount(pdfInfo.getFilePath());
                        pdfInfo.setPageCount(pageCount);
                    } catch (IOException ex) {
                        infoMessage = String.format("mergePdf-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                        logger.error(infoMessage, ex);
                    }
                }
                 */

                InputStream pdfStream = null;
                try {
                    pdfStream = pdfHelper.mergeFiles(inputList);
                } catch (IOException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                }

                File pdfFile = new File(pdfOutfile);
                try {
                    Files.copy(pdfStream, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                }

                try {
                    pdfStream.close();
                } catch (IOException ex) {
                    infoMessage = String.format("mergePdf-ActionHandler: IO-Fehler (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                }

                // Booksmarks in die erstellte Pdf-Datei eintragen
                if (!pdfHelper.setBookmarks(pdfOutfile, pdfInfoHashtable)) {
                    infoMessage = String.format("mergePdf-ActionHandler: Fehler beim Setzen der Bookmarks in %s", pdfOutfile);
                    logger.error(infoMessage);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, infoMessage, ButtonType.FINISH);
                    alert.setTitle("Fehler beim Setzen der Bookmarks.");
                    alert.setHeaderText("mergePdf-ActionHandler");
                    alert.showAndWait();
                        return;
                } else {
                    infoMessage = String.format("mergePdf-ActionHandler: Bookmarks in %s gesetzt", pdfOutfile);
                    logger.info(infoMessage);
                }

                // Progressbar soll wieder anhalten
                // progressBar.setProgress(0);
                // progressBar.setVisible(false);
                // vbox.getChildren().remove(progressBar);

                Instant endeZeit = Instant.now();
                String dauer = Duration.between(startZeit, endeZeit).toString();
                infoMessage = "Ausgeführt in: " + dauer;
                logger.info(infoMessage);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, infoMessage, ButtonType.FINISH);
                alert.setTitle("Pdf-Merge abgeschlossen");
                alert.setHeaderText("Die Ausgabedatei heißt " + pdfOutfile);
                alert.showAndWait();

                // Status-Meldung ausgeben
                lbl3.setText(String.format("Pdf-Merge nach %s wurde abgeschlossen.", pdfOutfile));

            }
        });

        menuAction.getItems().add(pdfMergeItem);

        Menu menuInfo = new Menu("Info");

        imgPfad = getClass().getResource("/images/info.png").toURI().toString();
        MenuItem aboutItem = new MenuItem("Über das Programm", new ImageView(new Image(imgPfad)));

        aboutItem.setOnAction(new EventHandler<ActionEvent>() {

             @Override
             public void handle(ActionEvent actionEvent) {
                 Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                 infoAlert.setTitle("Über das Programm");
                 infoAlert.setHeaderText(String.format("Portabler XJustiz-Viewer %s", appVersion));
                 infoAlert.setContentText("Alle Rechte vorbehalten usw.");
                 infoAlert.showAndWait();
             }
        });

        menuInfo.getItems().add(aboutItem);

        menuBar.getMenus().addAll(menuFile, menuAction, menuInfo);

        // Wichtg für MacOS, damit die App sich in das Systemmenü integrieren kann (?)
        menuBar.useSystemMenuBarProperty().set(true);

        // ((VBox) scene.getRoot()).getChildren().addAll(menuBar);

        stage.setOnShowing((event) -> {
            System.out.println("Showing Stage");
        });

        stage.setOnShown((event) -> {
            System.out.println("Shown Stage");
        });

        stage.setTitle(String.format("EF-XJustiz-Viewer %s", appVersion));
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Aktualisiert die Menüeinträge für "Zuletzt verwendet" basierend auf der lruList
     */
    private void updateLruMenuItems() {
        // Alle Menüeinträge zuerst ausblenden
        for (MenuItem item : lruItems) {
            item.setVisible(false);
        }
        
        // Dann die vorhandenen Einträge anzeigen
        for (int i = 0; i < lruList.size() && i < MAX_LRU_ENTRIES; i++) {
            String path = lruList.get(i);
            // Nur den Dateinamen als Text anzeigen (benutzerfreundlicher)
            File file = new File(path);
            String displayName = file.getName();
            
            lruItems[i].setText(displayName);
            // Vollständigen Pfad als UserData speichern für die Öffnen-Aktion
            lruItems[i].setUserData(path);
            lruItems[i].setVisible(true);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}