/*
  XJustiz-Pdf-Merge für Windows, MacOs und Linux
  Autor: Peter Monadjemi - pm@eureka-fach.de
  Letzte Änderung: 14/02/22
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

public class XEFPdfMerge extends Application {
    private String xJustizPfad;
    private String osName = "Unbekannt";
    private String appVersion = "0.30";
    // Kein Scope Modifier, daher Sichtbarkeit innerhalb des Package
    static  Logger logger = null; // LogManager.getLogger(XEFPdfMerge.class);
    private XmlHelper xmlHelper = null;
    private String xmlPfad = "";
    private String basePfad = "";
    private String infoMessage = "";
    private String pdfOutfile = "GesamtePDF.pdf";
    private String imgPfad = "";
    private Hashtable<String, PdfInfo> pdfInfoHashtable = null;
    private AppConfig config = null;

    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        logger = LogManager.getLogger(XEFPdfMerge.class);

        // Die Versionsnummer von Log4J loggen
        String log4JVersion = logger.getClass().getPackage().getSpecificationVersion();
        infoMessage = String.format("*** Using Log4J version %s ***", log4JVersion);
        logger.info(infoMessage);

        // User directory holen
        String userDir = System.getProperty("user.home");

        // OS-Name holen
        osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        infoMessage = String.format("XEFPdfMerge->start: osName=%s", osName);
        logger.info(infoMessage);

        // Ausgabeverzeichnis OS-spezifisch festlegen
        // Ist u.U. nicht erforderlich, da user.home bereits OS-spezifisch ist
        if (osName.contains("win")) {
            pdfOutfile = userDir + "/documents/" + pdfOutfile;
        } else if (osName.contains("mac")) {
            pdfOutfile = userDir + "/documents/" + pdfOutfile;
        } else {
            // Bei Linux Documents statt documents?
            pdfOutfile = userDir + "/Documents/" + pdfOutfile;
        }

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
            xJustizPfad = userDir + "/documents";
        }

        // Versionsnummer  aus der Config-Datei holen
        // if (config != null && config.getProperty("version") != null) {
        //     appVersion = config.getProperty("version");
        // }

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
        imgPfad = getClass().getResource("/images/nachrichtxml.png").toURI().toString();
        MenuItem openNachrichtXml = new MenuItem("Nachricht.xml öffnen", new ImageView(new Image(imgPfad)));

        openNachrichtXml.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                // Gibt es einen gespeicherten pfad
                if (config != null && config.getProperty("LastPathSelected") != null) {
                    String lastPath = config.getProperty("LastPathSelected");
                    fileChooser.setInitialDirectory(new File(lastPath));
                }

                // fileChooser.setInitialDirectory(new File(xJustizPfad));
                fileChooser.setTitle("Auswahl Nachricht.xml");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Xml-Dateien", "*.xml"),
                        new FileChooser.ExtensionFilter("Alle Dateien", "*.*")
                );

                File selectedFile = fileChooser.showOpenDialog(stage);
                if (selectedFile != null) {

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

                    String anzeigeName = "";;
                    String dateiName = "";
                    String datumErstellung  = "";

                    // Erfolgsmeldung ausgeben
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Hinweis");
                    alert.setHeaderText("");
                    alert.setContentText(xmlPfad + " wurde ausgewertet.");
                    alert.showAndWait();

                    // Pfad in "Statusbar" anzeigen
                    lbl3.setText(xmlPfad + " wurde geladen.");

                    // Anlegen der Bookmarks in der Ausgabe-Pdf
                    pdfInfoHashtable = new Hashtable<>();

                    try {
                        XmlHelper xmlHelper = new XmlHelper(logger, xmlPfad);
                        // Schema-Validierung
                        String xsdPfad = "schemas/xjustiz_0005_nachrichten_3_0.xsd";
                        List<String> validateErrors = xmlHelper.validateXMLSchema(xsdPfad, xmlPfad);
                        // Wenn es Validierungsfehler gab, dann alle loggen, aber die Ausführung geht weiter
                        if (validateErrors.size() == 0) {
                            infoMessage = "XSD-Schemavalidierung ohne Fehler";
                            logger.info(infoMessage);
                        } else {
                            for(String validateError: validateErrors) {
                                logger.warn(validateError);
                            }
                        }
                        PdfHelper pdfHelper = new PdfHelper(logger);
                        List<Akte> akten = xmlHelper.getAkten();
                        TreeItem triRoot = new TreeItem("Akten");
                        // Ins TreeView übertragen
                        for(Akte akte: akten) {
                            String aktenId = akte.getId();
                            TreeItem triAkte = new TreeItem("Akte=" + aktenId);
                            anzeigeName = akte.getAnzeigeName();
                            datumErstellung  = akte.getDatumErstellung();
                            triAkte.getChildren().add(new TreeItem("Anzeigename=" + anzeigeName));
                            triAkte.getChildren().add(new TreeItem("Aktentyp=" + akte.getAktenTyp()));
                            triAkte.getChildren().add(new TreeItem("Erstellungsdatum=" + datumErstellung));
                            // Alle Teilakten holen
                            List<Teilakte> teilakten = xmlHelper.getTeilakten(aktenId);
                            // Gibt es Teilakten?
                            if (teilakten.size() > 0) {
                                for(Teilakte teilakte: teilakten) {
                                    String teilakteId = teilakte.getId();
                                    TreeItem triTeilakte = new TreeItem("Teilakte=" + teilakteId);
                                    triTeilakte.getChildren().add(new TreeItem("Nummer im übg. Container=" + teilakte.getNummerImUebergeordnetenContainer()));
                                    // Alle Dokumente durchgehen
                                    List<Dokument> dokumente = xmlHelper.getDokumente(teilakteId, Aktentyp.Teilakte);
                                    for(Dokument dokument: dokumente) {
                                        TreeItem triDokument = new TreeItem("Dokument=" + dokument.getId());
                                        dateiName = dokument.getDateiname();
                                        triDokument.getChildren().add(new TreeItem("Pdf-Datei=" + dateiName));
                                        String pdfPfad = basePfad + "/" + dateiName;
                                        Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                        triDokument.getChildren().add(new TreeItem("Posteingangsdatum=" + dokument.getDatumPosteingang()));
                                        triDokument.getChildren().add(new TreeItem("Veraktungsdatum=" + dokument.getDatumVeraktung()));
                                        triDokument.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                        triTeilakte.getChildren().add(triDokument);
                                    }
                                    triAkte.getChildren().add(triTeilakte);
                                }
                            } else {
                                // Alle Dokumente der Akte durchgehen
                                List<Dokument> dokumente = xmlHelper.getDokumente(aktenId, Aktentyp.Akte);
                                for(Dokument dokument: dokumente) {
                                    TreeItem triDokument = new TreeItem("Dokument=" + dokument.getId());
                                    dateiName = dokument.getDateiname();
                                    triDokument.getChildren().add(new TreeItem("Pdf-Datei=" + dateiName));
                                    String pdfPfad = basePfad + "/" + dateiName;
                                    Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                    triDokument.getChildren().add(new TreeItem("Posteingangsdatum=" + dokument.getDatumPosteingang()));
                                    triDokument.getChildren().add(new TreeItem("Veraktungsdatum=" + dokument.getDatumVeraktung()));
                                    triDokument.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                    triAkte.getChildren().add(triDokument);
                                }
                            }

                            // TODO: Anzeigename und Datumsangaben als Bookmark setzen
                            // Hashtable mit Daten der Pdf-Datei aktualisieren
                            PdfInfo pdfInfo = new PdfInfo();
                            pdfInfo.setDisplayName(anzeigeName);
                            pdfInfo.setFileName(dateiName);
                            pdfInfo.getBookmarks().put("Erstellungsdatum", datumErstellung);
                            // TODO: Offenbar gibt es bei Java noch kein Pendant zu Combine()?
                            pdfInfo.setFilePath(basePfad + "/" + dateiName);
                            // TODO: Hier fehlt noch was?
                            // Eintrag in pdfInfoHashtable, damit das Setzen von Bookmarks später möglich ist
                            pdfInfoHashtable.put(dateiName, pdfInfo);

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

        // Trennlinie für das Menü
        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        imgPfad = getClass().getResource("/images/exit.png").toURI().toString();
        MenuItem exitItem =  new MenuItem("Beenden", new ImageView(new Image(imgPfad)));

        exitItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(1);
            }
        });

        menuFile.getItems().addAll(openNachrichtXml, sep1, exitItem);

        Menu menuAction = new Menu("Aktionen");
        imgPfad = getClass().getResource("/images/pdfmerge.png").toURI().toString();
        MenuItem pdfMerge = new MenuItem("GesamtPDF erstellen",  new ImageView(new Image(imgPfad)));

        // Ausführen der Merge-Aktion
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            private Instant startZeit = Instant.now();

            @Override
            public void handle(ActionEvent actionEvent) {

                lbl3.setText("Pdf-Merge wird gestartet.");
                lbl3.requestFocus();
                // Bringt leider nichts - Text wird erst am Ende angezeigt
                // TODO: Auslagern des Merge in einen Task oder einfacher per Platform.runLater()
                // https://riptutorial.com/javafx/example/7291/updating-the-ui-using-platform-runlater

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
                    try {
                        inputList.add(new FileInputStream(tmpPath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    infoMessage = String.format("mergePdf: %s hinzugefügt", tmpPath);
                    logger.info(infoMessage);
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
                pdfHelper.setBookmarks(pdfOutfile, pdfInfoHashtable);

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

        menuAction.getItems().add(pdfMerge);

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

    public static void main(String[] args) {
        launch();
    }
}