/*
    file: PdfMerger.java
    Letzte Aktualisierung: 2025-06-11
*/

package xpdfmergeV1;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.nio.file.Path;
import java.nio.file.Paths;
// import java.nio.file.Files;
// import java.nio.file.DirectoryStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

public class PdfMerger {
    private String infoMessage = "";
    private Logger logger = null;

    public PdfMerger(@SuppressWarnings("exports") Logger logger) {
        // Logger initialisieren, falls benötigt
        this.logger = logger;  
    }

    /**
     * 
     * Führt PDF-Dateien zusammen und erstellt automatisch Bookmarks basierend auf den Dateinamen
     */
    public void mergePDFsWithBookmarks(List<String> inputFiles, String basePath, String outputFile) throws IOException {
        PDDocument mergedDocument = new PDDocument();
        PDDocumentOutline outline = new PDDocumentOutline();
        mergedDocument.getDocumentCatalog().setDocumentOutline(outline);
        
        int currentPageIndex = 0;
                                                                                            List<PDDocument> openDocuments = new ArrayList<>();  // Sammle alle offenen Dokumente
        try {
            for (String inputFile : inputFiles) {
                // PDFBox braucht File, daher muss der String-Pfad in ein File-Objekt konvertiert werden
                // PDDocument document = PDDocument.load(Paths.get(inputFile)); 
                PDDocument document = Loader.loadPDF(Paths.get(basePath).resolve(inputFile).toFile()); 
                // Zur späteren Schließung sammeln
                openDocuments.add(document); 
                
                // Seiten zum merged Document hinzufügen
                for (PDPage page : document.getPages()) {
                    mergedDocument.addPage(page);
                }
                
                // Bookmark für diese PDF erstellen
                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setTitle(getFileNameWithoutExtension(inputFile));
                
                // Für die erste Seite einen anderen Ansatz
                if (currentPageIndex == 0) {
                    // Erste Seite: Verwende PDPageFitDestination
                    PDPageFitDestination destination = new PDPageFitDestination();
                    PDPage firstPageOfCurrentDoc = mergedDocument.getPage(currentPageIndex);
                    destination.setPage(firstPageOfCurrentDoc);
                    bookmark.setDestination(destination);
                } else {
                    // Alle anderen Seiten wie gewohnt
                    PDPageFitWidthDestination destination = new PDPageFitWidthDestination();
                    PDPage firstPageOfCurrentDoc = mergedDocument.getPage(currentPageIndex);
                    destination.setPage(firstPageOfCurrentDoc);
                    bookmark.setDestination(destination);
                }
                outline.addLast(bookmark);
                currentPageIndex += document.getNumberOfPages();
            }
        } catch (IOException ex) {
            // Fehler beim Laden oder Verarbeiten der PDF-Dateien
            infoMessage = String.format("mergePDFsWithBookmarks: Fehler beim Zusammenführen der PDF-Dateien: %s", ex.getMessage());
            logger.error(infoMessage);
            // Weiterwerfen, damit der Aufrufer den Fehler behandeln kann
            throw ex; 
        }
        
        // Erst speichern, dann alle Dokumente schließen
        try {
            mergedDocument.save(outputFile);
            mergedDocument.close();
        } catch (IOException ex) {
            // Fehler beim Speichern des zusammengeführten Dokuments
            infoMessage = String.format("mergePDFsWithBookmarks: Fehler beim Speichern der zusammengeführten PDF-Datei: %s", ex.getMessage());
            logger.error(infoMessage);
             // Weiterwerfen, damit der Aufrufer den Fehler behandeln kann
            throw ex; 
        }
        
        // Jetzt alle geöffneten Dokumente schließen
        try {
        for (PDDocument doc : openDocuments) {
                doc.close();
            }
        } catch (IOException ex) {
            // Fehler beim Schließen der geöffneten Dokumente
            infoMessage = String.format("mergePDFsWithBookmarks: Fehler beim Schließen der geöffneten PDF-Dokumente: %s", ex.getMessage());
            logger.error(infoMessage);
            // Weiterwerfen, damit der Aufrufer den Fehler behandeln kann
            throw ex;
        }
        
        infoMessage = String.format("mergePDFsWithBookmarks: \"PDF-Dateien erfolgreich zusammengeführt %s", outputFile);
        logger.info(infoMessage);
    }

    /**
     * Hilfsmethode zum Extrahieren des Dateinamens ohne Erweiterung
     */
    private String getFileNameWithoutExtension(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
    

    
}