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

            // Für alle Seitene eine Bookmark setzen
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

    public void setBookmarks(String pdfOutfile, Hashtable<String, PdfInfo> pdfInfoHashtable) {
        infoMessage = String.format("setBookmarks: Aufruf");
        this.logger.info(infoMessage);

        String pdfOutfileBak = "";
        Path destPath = null;
        Path sourcePath = null;

        // TODO: Alle Bookmarks in einer Datei setzen - am besten am Anfang eine Kopie anlegen, in die alle Bookmarks geschrieben werden
        // und die am Ende auf die Originaldatei kopiert wird
        try {
            // Pdf-Datei kopieren
            File file1 = new File(pdfOutfile);
            // Erweiterung ersetzen
            // pdfOutfileBak = pdfOutfile.replace("pdf", "pdfbak");
            // Datei kopieren
            // ? Path oder String für Files.copy?
            // sourcePath = Paths.get(pdfOutfile);
            // destPath = Paths.get(pdfOutfileBak);
            // Files.copy(sourcePath, destPath, REPLACE_EXISTING);

            // PDF-Dokument öffnen
            File pdfFile = new File(pdfOutfile);
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

            Integer pageCounter = 1;

            // forEach ist nicht geeignet, da keine lokale Variable in dem Lambda verwendet werden kann?
            // pdfInfoHashtable.forEach((String fileName, PdfInfo pdfInfo) -> {
            Enumeration en = pdfInfoHashtable.elements();

            while(en.hasMoreElements()) {
                PdfInfo pdfInfo = (PdfInfo) en.nextElement();
                // Bookmark setzen
                String bookmarkText1 = pdfInfo.getDisplayName();
                PDPageDestination pageDestination = new PDPageFitWidthDestination();
                PDPage page = pdfDoc.getPage(pageCounter);
                pageDestination.setPage(page);

                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setDestination(pageDestination);
                bookmark.setTitle(bookmarkText1);
                pagesOutline.addLast(bookmark);

                infoMessage = String.format("Bookmark auf Seite %d gesetzt.", pageCounter);
                logger.info(infoMessage);

                // Seitenzähler auf die nächste Startseite eines Teildokuments
                pageCounter += pdfInfo.getPageCount();

            };

            // Dokument wieder speichern - das Originaldokument aber zuvor löschen
            try {
                Files.delete(pdfFile.toPath());
            } catch (NoSuchFileException ex) {
                System.err.format("%s gibt es leider nicht.", pdfOutfile);
            } catch (IOException ex) {
                System.err.format("Allgemeiner Fehler beim Löschen von %s", pdfOutfile);
            }

            try {
                pdfDoc.save(pdfOutfile);
                infoMessage = String.format("%s wurde mit Bookmarks gespeichert.", pdfOutfile);
                logger.info(infoMessage);

            } catch (IOException ex) {
                infoMessage = String.format("Allgemeiner Fehler beim Speichern von %s", pdfOutfile);
                logger.error(infoMessage);
            }


            // Originaldatei löschen
            // Files.delete(sourcePath);
            // infoMessage = String.format("*** %s wurde gelöscht. ***", sourcePath);
            // logger.info(infoMessage);

            // Bak-Datei in Originaldatei umbenennen
            // Files.move(destPath, sourcePath);
            // infoMessage = String.format("*** %s wurde in %s umbenannt. ***", destPath, sourcePath);
            // logger.info(infoMessage);

        } catch (Exception ex) {
            infoMessage = String.format("setBookmarks: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
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
        try {
            PDDocument pdfDoc = Loader.loadPDF(new File(pdfPfad));
            pageCount = pdfDoc.getNumberOfPages();
        } catch (Exception ex) {
            infoMessage = String.format("getPdfPageCount: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return pageCount;
    }

}
