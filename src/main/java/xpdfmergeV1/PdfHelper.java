/*
file: PdfHelper.java
*/

package xpdfmergeV1;

import javafx.scene.control.ProgressBar;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PageMode;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.file.*;

import java.util.*;

// import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Logger;

/*
* Stellt allgemeine Pdf-Funktionen zur Verfügung (auf der Basis von PdfBox)
 * @author Pemo
 */
public class PdfHelper {
    private Logger logger;
    // private ProgressBar progressBar;
    private String infoMessage = "";

    public PdfHelper(Logger logger) {
        this.logger = logger;
    }

    /*
    public PdfHelper(Log logger, ProgressBar progressBar) {
        this.logger = logger;
        this.progressBar = progressBar;
    }
    */

    private PDFMergerUtility createPdfMerger(List<InputStream> sources, ByteArrayOutputStream mergedPdfOutputstream) {
        infoMessage = "createPdfMerger: Initializing PDF Merge Utilty";
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        try {
            this.logger.info(infoMessage);
            pdfMerger.addSources(sources);
            pdfMerger.setDestinationStream(mergedPdfOutputstream);
        } catch (Exception ex) {
            infoMessage = String.format("createPdfMerger: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return pdfMerger;
    }

    /**
     * createPdfDocumentInfo
     * @param title
     * @param creator
     * @param subject
     * @return
     */
    private PDDocumentInformation createPdfDocumentInfo(String title, String creator, String subject) {
        infoMessage = "createPdfDocumentInfo: Aufruf";
        this.logger.info(infoMessage);
        PDDocumentInformation documentInformation = new PDDocumentInformation();
        try {
            documentInformation.setTitle(title);
            documentInformation.setCreator(creator);
            documentInformation.setSubject(subject);
        } catch (Exception ex) {
            infoMessage = String.format("createPdfDocumentInfo: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return documentInformation;
    }

    /**
     * createXMPMetadata()
     * @param cosStream
     * @param title
     * @param creator
     * @param subject
     * @return
     * @throws BadFieldValueException
     * @throws TransformerException
     * @throws IOException
     */
    private PDMetadata createXMPMetadata(COSStream cosStream, String title, String creator, String subject)
            throws BadFieldValueException, TransformerException, IOException  {
        infoMessage = String.format("createXMPMetadata: Aufruf mit XMP metadata (%s,%s,%s,) for merged PDF",title, creator, subject);
        this.logger.info(infoMessage);
        PDMetadata pdMetadata = null;
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        try {

            // PDF/A-1b properties
            PDFAIdentificationSchema pdfaSchema = metadata.createAndAddPFAIdentificationSchema();
            pdfaSchema.setPart(1);
            pdfaSchema.setConformance("B");
    
            // Dublin Core properties
            DublinCoreSchema coreSchema = metadata.createAndAddDublinCoreSchema();
            coreSchema.setTitle(title);
            coreSchema.addCreator(creator);
            coreSchema.addSubject(subject);
    
            // XMP Basic Properties
            XMPBasicSchema basicSchema = metadata.createAndAddXMPBasicSchema();
            Calendar creationDate = Calendar.getInstance();
            basicSchema.setCreateDate(creationDate);
            basicSchema.setModifierDate(creationDate);
            basicSchema.setMetadataDate(creationDate);
            basicSchema.setCreatorTool(creator);
    
            // Create and return XMP data structure in XML Format
            try(ByteArrayOutputStream xmpOutputStream =  new ByteArrayOutputStream();
                OutputStream cosXMPStream = cosStream.createOutputStream())
            {
                new XmpSerializer().serialize(metadata, xmpOutputStream, true);
                cosXMPStream.write(xmpOutputStream.toByteArray());
                pdMetadata = new PDMetadata(cosStream);
            } catch(Exception ex) {
                infoMessage = String.format("createXMPMetadata: Fehler beim Schreiben der Metadaten (%s)", ex.getMessage());
                logger.error(infoMessage, ex);
            }
        } catch (Exception ex) {
            infoMessage = String.format("createXMPMetadata: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return pdMetadata;
    }

    public InputStream mergeFiles(final List<InputStream> sources) throws IOException
    {
        infoMessage = String.format("mergeFiles: Aufruf");
        this.logger.info(infoMessage);

        String title = "Zusammenfassung";
        String creator = "Eureka-Fach";
        String subject = "XJustiz";

        try(COSStream cosStream = new COSStream();
            ByteArrayOutputStream mergedPdfOutputStream = new ByteArrayOutputStream())
        {
            PDFMergerUtility pdfMerger = this.createPdfMerger(sources, mergedPdfOutputStream);
            PDDocumentInformation pdfDocumentInfo = createPdfDocumentInfo(title, creator, subject);
            PDMetadata pdfMetadata = this.createXMPMetadata(cosStream, title, creator, subject);

            pdfMerger.setDestinationDocumentInformation(pdfDocumentInfo);
            pdfMerger.setDestinationMetadata(pdfMetadata);

            infoMessage = String.format("mergeFiles: mergin: %d source documents into one PDF.",sources.size());
            this.logger.info(infoMessage);

            // Dieser Aufruf dauert sehr bis relativ lange und sollte auf einem Backgroundthread ausgeführt werden
            pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            infoMessage = String.format("mergeFiles: PDF merge sucessfull, size=%d Bytes.", mergedPdfOutputStream.size());
            this.logger.info(infoMessage);

            return new ByteArrayInputStream(mergedPdfOutputStream.toByteArray());


        } catch(BadFieldValueException | TransformerException ex) {
            infoMessage = "mergeFiles: PDF merge problem";
            throw new IOException(infoMessage, ex);
        } finally {
            sources.forEach(IOUtils::closeQuietly);
        }

    }

    /*
    * https://stackoverflow.com/questions/23326562/convert-pdf-files-to-images-with-pdfbox
     */
    public String ConvertPdf2Img(String fileInput, String imgOutput) throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /*
    * Setzen eines Lesezeichens (noch nicht getestet)
    * Kann wieder raus, da die Bookmarks in der Gesamt-Pdf-Datei gesetzt werden
     */

    public void setBookmark(String pdfPfad, String bookmarkText) throws IOException {
        infoMessage = String.format("setBookmark: Aufruf");
        this.logger.info(infoMessage);

        try {
            // File-Objekt anlegen
            File pdfFile = new File(pdfPfad);

            // PDF-Dokument öffnen
            PDDocument pdfDoc = Loader.loadPDF(pdfFile);

            // Bookmark für alle Seiten
            PDDocumentOutline rootOutline = new PDDocumentOutline();

            // Outline mit Dokument verknüpfen
            pdfDoc.getDocumentCatalog().setDocumentOutline(rootOutline);

            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("Alle Seiten");

            rootOutline.addLast(pagesOutline);

            pagesOutline.openNode();
            rootOutline.openNode();

            pdfDoc.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

            // Für alle Seiten eine Bookmark setzen
            for(int i=0;i<pdfDoc.getNumberOfPages();i++) {
                PDPageDestination pageDestination = new PDPageFitWidthDestination();
                pageDestination.setPage(pdfDoc.getPage(i));

                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setDestination(pageDestination);
                bookmark.setTitle(bookmarkText);
                pagesOutline.addLast(bookmark);
            }

            // Dokument wieder speichern - das Originaldokument aber zuvor löschen
            try {
                Files.delete(pdfFile.toPath());
            } catch (NoSuchFileException ex) {
                infoMessage = String.format("%s gibt es leider nicht.", pdfPfad);
                logger.error(infoMessage);
            } catch (IOException ex) {
                infoMessage = String.format("Allgemeiner Fehler beim Löschen von %s", pdfPfad);
                logger.error(infoMessage);
            }

            try {
                pdfDoc.save(pdfFile);
                infoMessage = String.format("Bookmarks für %s gesetzt." , pdfPfad);
                logger.error(infoMessage);
            } catch (IOException ex) {
                infoMessage = String.format("Allgemeiner Fehler beim Speichern von %s", pdfPfad);
                logger.error(infoMessage);
            }
        } catch (Exception ex) {
            infoMessage = String.format("setBookmark: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
    }

    /**
     * Setzen der Lesemarken in der Gesamt-Pdfdatei
     * @param pdfOutfile
     * @param pdfInfoHashtable
     * @return true wenn erfolgreich, false im Fehlerfall
     */
    public boolean setBookmarks(String pdfOutfile, LinkedHashMap<AkteInfo, List<PdfDocumentInfo>> pdfInfoHashtable) {
        infoMessage = String.format("setBookmarks: Aufruf ");
        this.logger.info(infoMessage);

        // Check if the file exists before proceeding
        File pdfFile = new File(pdfOutfile);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            infoMessage = String.format("setBookmarks: Die Datei %s existiert nicht oder ist keine Datei.", pdfOutfile);
            logger.error(infoMessage);
            return false; // Exit the method if the file doesn't exist
        }

        // Check if the file is 0 bytes in size
        if (pdfFile.length() == 0) {
            infoMessage = String.format("setBookmarks: Die Datei %s ist 0 Bytes groß und kann keine Bookmarks enthalten.", pdfOutfile);
            logger.error(infoMessage);
            return false; // Exit the method if the file is empty
        }

        // Check if hashtable is empty or null
        if (pdfInfoHashtable == null || pdfInfoHashtable.isEmpty()) {
            infoMessage = "setBookmarks: Keine Bookmark-Informationen vorhanden (pdfInfoHashtable ist leer oder null)";
            logger.error(infoMessage);
            return false; // Exit the method if there's no data to process
        }

        String pdfOutfileBak = "";
        Path destPath = null;
        Path sourcePath = null;
        PDDocument pdfDoc = null;

        // TODO: Alle Bookmarks in einer Datei setzen - am besten am Anfang eine Kopie anlegen, in die alle Bookmarks geschrieben werden
        // und die am Ende auf die Originaldatei kopiert wird
        try {
            // PDF-Dokument öffnen
            try {
                pdfDoc = Loader.loadPDF(pdfFile);
            } catch (Exception ex) {
                infoMessage = String.format("setBookmarks: Datei %s kann nicht geladen werden (%s)", pdfOutfile, ex.getMessage());
                logger.error(infoMessage, ex);
                // Exit if we can't load the PDF
                return false; // Exit if the PDF cannot be loaded
            }
            
            if (pdfDoc.getNumberOfPages() <= 0) {
                infoMessage = "setBookmarks: Das PDF-Dokument enthält keine Seiten";
                logger.error(infoMessage);
                try {
                    pdfDoc.close();
                } catch (Exception closeEx) {
                    logger.error("setBookmarks: Fehler beim Schließen des leeren PDFs", closeEx);
                }
                return false;
            }

            // Bookmark für alle Seiten
            PDDocumentOutline rootOutline = new PDDocumentOutline();

            // Outline mit Dokument verknüpfen
            pdfDoc.getDocumentCatalog().setDocumentOutline(rootOutline);

            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("Alle Seiten");

            rootOutline.addLast(pagesOutline);

            pagesOutline.openNode();
            rootOutline.openNode();

            pdfDoc.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);

            // Integer pageCounter = 0;
            int currentPageNumber = 0;
            int aktePageNumber = 0;
            int totalPageCount = pdfDoc.getNumberOfPages();

            // Alle Akten durchgehen
            Set<AkteInfo> akteInfoSet = pdfInfoHashtable.keySet();
            for(AkteInfo akte : akteInfoSet) {
                if (akte == null) {
                    infoMessage = "setBookmarks: Null-AkteInfo gefunden, überspringe";
                    logger.error(infoMessage);
                    continue;
                }
                
                // Die Seitenzahl der Akte ist die aktuelle Seitennummer in Bezug auf die Gesamt-Pdf
                aktePageNumber = currentPageNumber;
                
                // Sicherheitscheck - Ist die Seitennummer valide?
                if (aktePageNumber >= totalPageCount) {
                    infoMessage = String.format("setBookmarks: Seitennummer %d außerhalb des gültigen Bereichs (max: %d)", 
                        aktePageNumber, totalPageCount - 1);
                    logger.error(infoMessage);
                    continue;
                }
                
                try {
                    // Bookmark für Akte setzen
                    PDPageDestination pageDestinationAkte = new PDPageFitWidthDestination();
                    // Seite in Bezug auf das Gesamtdokument holen
                    PDPage page = pdfDoc.getPage(aktePageNumber);
                    pageDestinationAkte.setPage(page);

                    PDOutlineItem bookmarkAkte = new PDOutlineItem();
                    bookmarkAkte.setDestination(pageDestinationAkte);
                    bookmarkAkte.setTitle(akte.getAnzeigeName());

                    // Jetzt die Bookmarks für das Dokument setzen
                    List<PdfDocumentInfo> infoListe = pdfInfoHashtable.get(akte);
                    if (infoListe != null && !infoListe.isEmpty()) {
                        // Alle Dokumente durchgehen
                        for(var pdfInfo : infoListe) {
                            if (pdfInfo == null) {
                                infoMessage = "setBookmarks: Null-PdfDocumentInfo gefunden, überspringe";
                                logger.error(infoMessage);
                                continue;
                            }
                            
                            // Seitenzahl für das aktuelle Dokument abrufen
                            int pageCount = pdfInfo.getPageCount() != null ? pdfInfo.getPageCount() : 0;
                            
                            // Sicherheitscheck - Ist die Seitennummer valide?
                            if (currentPageNumber >= totalPageCount) {
                                infoMessage = String.format("setBookmarks: Dokument-Seitennummer %d außerhalb des gültigen Bereichs (max: %d)", 
                                    currentPageNumber, totalPageCount - 1);
                                logger.error(infoMessage);
                                continue;
                            }
                            
                            try {
                                // Bookmark für das Dokument anlegen
                                PDOutlineItem bookmarkDokument = new PDOutlineItem();
                                PDPageDestination pageDestinationDokument = new PDPageFitWidthDestination();
                                // Erste Seite des aktuellen Dokuments holen
                                page = pdfDoc.getPage(currentPageNumber);
                                pageDestinationDokument.setPage(page);
                                bookmarkDokument.setTitle(pdfInfo.getDisplayName());
                                bookmarkDokument.setDestination(pageDestinationDokument);

                                // Detail-Bookmarks für das Dokument setzen
                                Hashtable<String, String> htBookmarks = pdfInfo.getBookmarks();
                                if (htBookmarks != null) {
                                    Enumeration enKeys = htBookmarks.keys();
                                    while(enKeys.hasMoreElements()) {
                                        String bmName = enKeys.nextElement().toString();
                                        String bmText = htBookmarks.get(bmName);
                                        PDOutlineItem bm = new PDOutlineItem();
                                        bm.setDestination(pageDestinationDokument);
                                        bm.setTitle(bmName + "=" + bmText);
                                        bookmarkDokument.addLast(bm);
                                    }
                                }
                                
                                // dokument-Bookmark an die Akte-Bookmark anhängen
                                bookmarkAkte.addLast(bookmarkDokument);
                                infoMessage = String.format("setBookmarks: Bookmark für Dokument auf Seite %d gesetzt.", currentPageNumber);
                                logger.info(infoMessage);
                                
                                // Seitenzähler auf die nächste Startseite eines Teildokuments
                                currentPageNumber += pageCount;
                            } catch (Exception ex) {
                                infoMessage = String.format("setBookmarks: Fehler beim Setzen einer Bookmark auf Seite %d (%s)", 
                                    currentPageNumber, ex.getMessage());
                                logger.error(infoMessage, ex);
                                // Trotzdem weitermachen mit dem nächsten Dokument
                                currentPageNumber += pageCount > 0 ? pageCount : 1;
                            }
                        }
                    }
                    
                    // Bookmark für Akte setzen
                    pagesOutline.addLast(bookmarkAkte);
                    infoMessage = String.format("setBookmarks: Bookmark für Akte auf Seite %d gesetzt.", aktePageNumber);
                    logger.info(infoMessage);
                } catch (Exception ex) {
                    infoMessage = String.format("setBookmarks: Fehler beim Setzen der Bookmarks für Akte %s (%s)", 
                        akte.getAnzeigeName(), ex.getMessage());
                    logger.error(infoMessage, ex);
                }
            }

            // Dokument wieder speichern - das Originaldokument aber zuvor sichern statt löschen
            boolean saveSuccessful = false;
            
            // Create a temp file for saving
            File tempFile = null;
            try {
                tempFile = File.createTempFile("pdfbookmark_", ".pdf");
                // Save to temp file first to avoid losing the original if saving fails
                pdfDoc.save(tempFile);
                saveSuccessful = true;
            } catch (IOException ex) {
                infoMessage = String.format("setBookmarks: Fehler beim Speichern in temporäre Datei (%s)", ex.getMessage());
                logger.error(infoMessage, ex);
                return false;
            }
            
            // Only if saving to temp was successful, replace the original
            if (saveSuccessful && tempFile != null && tempFile.exists()) {
                try {
                    // Create backup of original file (optional)
                    File backupFile = new File(pdfOutfile + ".bak");
                    try {
                        Files.copy(pdfFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        // Not critical, just log and continue
                        logger.warn("setBookmarks: Backup-Datei konnte nicht erstellt werden: " + ex.getMessage());
                    }
                    
                    // Replace original with the new file
                    Files.copy(tempFile.toPath(), pdfFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    infoMessage = String.format("setBookmarks: %s wurde mit Bookmarks gespeichert.", pdfOutfile);
                    logger.info(infoMessage);
                    
                    // Clean up temp file
                    try {
                        Files.delete(tempFile.toPath());
                    } catch (IOException ex) {
                        // Not critical, just log
                        logger.warn("setBookmarks: Temporäre Datei konnte nicht gelöscht werden: " + ex.getMessage());
                    }
                    return true;
                } catch (IOException ex) {
                    infoMessage = String.format("setBookmarks: Fehler beim Ersetzen der Originaldatei %s (%s)", 
                        pdfOutfile, ex.getMessage());
                    logger.error(infoMessage, ex);
                    return false;
                }
            }
            return false;
        } catch (Exception ex) {
            infoMessage = String.format("setBookmarks: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
            return false;
        } finally {
            // Make sure we always close the document
            if (pdfDoc != null) {
                try {
                    pdfDoc.close();
                } catch (IOException ex) {
                    logger.error("setBookmarks: Fehler beim Schließen des PDFs", ex);
                }
            }
        }
    };

    /*
    ** Gibt die Seitenzahlen aller Pdf-Dateien als Hashtable zurück
     */
    public Hashtable<String, Integer> getPdfPageCount(List<String> pdfDateien) throws IOException {
        infoMessage = String.format("getPdfPageCount: Aufruf");
        this.logger.info(infoMessage);

        Hashtable<String, Integer> tmpTable = new Hashtable<>();
        try {
            for(String pdfDatei: pdfDateien) {
                try {
                    PDDocument pdfDoc = Loader.loadPDF(new File(pdfDatei));
                    Path filePath = Paths.get(pdfDatei);
                    String fileName = filePath.getFileName().toString();
                    Integer pageCount = pdfDoc.getNumberOfPages();
                    tmpTable.put(fileName, pageCount);
                } catch(Exception ex) {
                    infoMessage = String.format("getPdfPageCount - Seitenzahl für %s kann nicht abgefragt werden (%s)",
                      pdfDatei, ex.getMessage());
                    logger.error(infoMessage, ex);
                }
            }
        } catch (Exception ex) {
            infoMessage = String.format("getPdfPageCount: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return tmpTable;
    }

    public Integer getPdfPageCount(String pdfPfad) throws IOException {
        infoMessage = String.format("getPdfPageCount: Aufruf");
        int pageCount = 0;
        this.logger.info(infoMessage);
        
        File pdfFile = new File(pdfPfad);
        if (!pdfFile.exists() || !pdfFile.isFile()) {
            infoMessage = String.format("getPdfPageCount: Die Datei %s existiert nicht oder ist keine Datei.", pdfPfad);
            logger.warn(infoMessage);
            return pageCount; // Return 0 if file doesn't exist
        }
        
        try {
            PDDocument pdfDoc = Loader.loadPDF(pdfFile);
            pageCount = pdfDoc.getNumberOfPages();
            pdfDoc.close(); // Close the document to free resources
        } catch (Exception ex) {
            infoMessage = String.format("getPdfPageCount: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return pageCount;
    }

}
