package pdfmergev1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class XEFPdfMerge extends Application {
    // TODO: Aus config-Datei auslesen
    private String xJustizPfad = "D:\\EurekaFach\\BEAkten-Projekt2021\\baakte";
    private static final Log logger = LogFactory.getLog(XEFPdfMerge.class);
    private XmlHelper xmlHelper = null;
    private String xmlPfad = "";
    private String basePfad = "";
    private String infoMessage = "";
    private String pdfOutfile = "GesamtePDF.pdf";
    private Hashtable<String, PdfInfo> pdfInfoHashtable = null;

    @Override
    public void start(Stage stage) throws IOException {
        // User directory holen
        String userDir = System.getProperty("user.home");

        // Funktioniert so nur unter Windows
        pdfOutfile = userDir + "/documents/" + pdfOutfile;

        // xJustiz-Pfad einlesen
        AppConfig config = new AppConfig();
        xJustizPfad = config.getProperty("xJustizPfad");

        infoMessage = String.format("*** xJustizPfad=%s ***", xJustizPfad);
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

        // hbox für TreeView anlegen
        // HBox hBox = new HBox();

        // hBox.setPadding(new Insets(50, 5 , 5, 50));
        vbox2.setPadding(new Insets(10, 5 , 5, 10));

        // TreeView anlegen
        TreeView trvAkten = new TreeView();

        // ???
        trvAkten.setPadding(new Insets(10, 20, 10, 20));

        // TreeView zur HBox hinzufügen
        // hBox.getChildren().add(trvDokumente);

        vbox2.getChildren().addAll(lbl1, trvAkten);

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

        MenuItem openNachrichtXml = new MenuItem("Nachricht.xml öffnen",
         new ImageView(new Image("file:images/nachrichtxml.png")));

        openNachrichtXml.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(xJustizPfad));
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

                    String anzeigeName = "";
                    String dateiName = "";

                    // Erfolgsmeldung ausgeben
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Hinweis");
                    alert.setHeaderText("");
                    alert.setContentText(xmlPfad + " wurde ausgewertet.");
                    alert.showAndWait();

                    // Anlegen der Bookmarks in der Ausgabe-Pdf
                    pdfInfoHashtable = new Hashtable<>();

                    try {
                        XmlHelper xmlHelper = new XmlHelper(logger, xmlPfad);
                        PdfHelper pdfHelper = new PdfHelper(logger);
                        List<Akte> akten = xmlHelper.getAkten();
                        TreeItem triRoot = new TreeItem("Akten");
                        // Ins TreeView übertragen
                        for(Akte akte: akten) {
                            String aktenId = akte.getId();
                            TreeItem triAkte = new TreeItem(aktenId);
                            anzeigeName = akte.getAnzeigeName();
                            triAkte.getChildren().add(new TreeItem("Anzeigename=" + anzeigeName));
                            triAkte.getChildren().add(new TreeItem("Aktentyp=" + akte.getAktenTyp()));
                            // Alle Teilakten holen
                            List<Teilakte> teilakten = xmlHelper.getTeilakten(aktenId);
                            // Gibt es Teilakten?
                            if (teilakten.size() > 0) {
                                for(Teilakte teilakte: teilakten) {
                                    triAkte.getChildren().add(new TreeItem("Nummer im übg. Container=" + teilakte.getNummerImUebergeordnetenContainer()));
                                    // Alle Dokumente durchgehen
                                    String teilaktenId = teilakte.getId();
                                    List<Dokument> dokumente = xmlHelper.getDokumente(teilaktenId, Aktentyp.Teilakte);
                                    for(Dokument dokument: dokumente) {
                                        dateiName = dokument.getDateiname();
                                        triAkte.getChildren().add(new TreeItem("Pdf-Datei=" + dateiName));
                                        // TODO: Nur provisorisch, der basePfad muss anders festgelegt werden
                                        basePfad = xJustizPfad;
                                        String pdfPfad = basePfad + "/" + dateiName;
                                        Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                        triAkte.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                        triRoot.getChildren().add(triAkte);
                                    }
                                }
                            } else {
                                // Alle Dokumente der Akte durchgehen
                                List<Dokument> dokumente = xmlHelper.getDokumente(aktenId, Aktentyp.Akte);
                                for(Dokument dokument: dokumente) {
                                    dateiName = dokument.getDateiname();
                                    triAkte.getChildren().add(new TreeItem("Pdf-Datei=" + dateiName));
                                    // TODO: Nur provisorisch, der basePfad muss anders festgelegt werden
                                    basePfad = xJustizPfad;
                                    String pdfPfad = basePfad + "/" + dateiName;
                                    Integer pageCount = pdfHelper.getPdfPageCount(pdfPfad);
                                    triAkte.getChildren().add(new TreeItem("Anzahl Seiten=" + pageCount));
                                    triRoot.getChildren().add(triAkte);
                                }
                            }

                            // Hashtable mit Daten der Pdf-Datei aktualisieren
                            PdfInfo pdfInfo = new PdfInfo();
                            pdfInfo.setDisplayName(anzeigeName);
                            pdfInfo.setFileName(dateiName);
                            // TODO: Offenbar gibt es bei Java noch kein Pendant zu Combine()?
                            pdfInfo.setFilePath(basePfad + "/" + dateiName);
                            // TODO: Hier fehlt noch was?
                            // Eintrag in pdfInfoHashtable, damit das Setzen von Bookmarks später möglich ist
                            pdfInfoHashtable.put(dateiName, pdfInfo);
                        }
                        // Root-Element des TreeView hinzufügen
                        trvAkten.setRoot(triRoot);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Trennlinie für das Menü
        SeparatorMenuItem sep1 = new SeparatorMenuItem();

        MenuItem exitItem =  new MenuItem("Beenden",
            new ImageView(new Image("file:images/exit.png")));;

        exitItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent actionEvent) {
                System.exit(1);
            }
        });

        menuFile.getItems().addAll(openNachrichtXml, sep1, exitItem);

        Menu menuAction = new Menu("Aktionen");
        MenuItem pdfMerge = new MenuItem("GesamtPDF erstellen",
                new ImageView(new Image("file:images/pdfmerge.png")));

        // Ausführen der Merge-Aktion
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            private Instant startZeit = Instant.now();


            @Override
            public void handle(ActionEvent actionEvent) {

                // Pfade aller Pdf-Dateien aus der Xml-Datei ziehen
                try {
                    xmlHelper = new XmlHelper(logger, xmlPfad);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
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
                    infoMessage = String.format("*** Hinzugefügt: %s", tmpPath);
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
                        ex.printStackTrace();
                    }

                }

                InputStream pdfStream = null;
                try {
                    pdfStream = pdfHelper.mergeFiles(inputList);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                File pdfFile = new File(pdfOutfile);
                try {
                    Files.copy(pdfStream, pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                try {
                    pdfStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
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
            }
        });

        menuAction.getItems().add(pdfMerge);

        Menu menuInfo = new Menu("Info");
        MenuItem aboutItem = new MenuItem("Über das Programm",
                new ImageView(new Image("file:images/info.png")));

        aboutItem.setOnAction(new EventHandler<ActionEvent>() {

             @Override
             public void handle(ActionEvent actionEvent) {
                 Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                 infoAlert.setTitle("Über das Programm");
                 infoAlert.setHeaderText("Portabler XJustiz-Viewer 0.1");
                 infoAlert.setContentText("Alle Rechte vorbehalten usw.");
                 infoAlert.showAndWait();
             }
        });

        menuInfo.getItems().add(aboutItem);

        menuBar.getMenus().addAll(menuFile, menuAction, menuInfo);

        // ((VBox) scene.getRoot()).getChildren().addAll(menuBar);

        stage.setOnShowing((event) -> {
            System.out.println("Showing Stage");
        });

        stage.setOnShown((event) -> {
            System.out.println("Shown Stage");
        });

        stage.setTitle("EF-XJustiz-Viewer 0.1");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}