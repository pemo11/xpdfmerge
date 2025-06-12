/*
file: XmlHelper.java
*/
package xpdfmergeV1;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// import org.apache.commons.logging.Log;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/*
 * Stellt allgemeine Xml-Funktionen für eine bestimmte Xml-Datei
 * zur Verfügung (auf der Basis von javax.xml.parsers)
 * @author Pemo
 */
public class XmlHelper {
    private Logger logger;
    private String xmlPfad;
    private Document xDoc = null;
    private String nsName = "http://www.xjustiz.de";
    private String infoMessage = "";

    /**
     * Konstruktor für die Initalisierung
     * @param logger
     * @param xmlPfad
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @SuppressWarnings("exports")
    public XmlHelper(Logger logger, String xmlPfad) throws IOException, ParserConfigurationException, SAXException {
        this.logger = logger;
        this.xmlPfad = xmlPfad;
        try {
            // Xml-Datei öffnen
            File xmlFile = new File(xmlPfad);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);

            // DocumentBuilder für das Parsen der Xml-Datei anlegen
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            xDoc = dBuilder.parse(xmlFile);
            // Kontrollmeldung
            infoMessage = String.format("XML-Datei %s wurde geparsed", xmlPfad);
            this.logger.info(infoMessage);
            infoMessage = String.format("XML-Namespace=%s", xDoc.getDocumentElement().getPrefix());
            this.logger.info(infoMessage);
        } catch (Exception ex) {
            infoMessage = String.format("XmlHelper-Konstruktor: Allgemeiner Fehler (%s)", ex.getMessage());
            this.logger.error(infoMessage, ex);
        }
    }

    /**
     *  Holt alle Elemente eines bestimmten Typs
     * @param tagName
     * @param nsName
     * @return List<Element> der Elemente
     */
    public List<Element> getElements(String tagName, String nsName) {
        infoMessage = String.format("Aufruf von getElements mit tagName=%s und nsName=%s", tagName, nsName);
        this.logger.info(infoMessage);
        List<Element> tmpListe = new ArrayList<Element>();
        try {
            Element root = xDoc.getDocumentElement();
            NodeList nlList = root.getElementsByTagNameNS(nsName, tagName);
            // Alle nodes einzeln ansprechen
            for(int i=0; i<nlList.getLength();i++) {
                Node nNode = nlList.item(i);
                // Ist das Element ein Node?
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    tmpListe.add((Element)nNode);
                }
            }
            infoMessage = String.format("getElements: Abschluss mit %d Elementen.", tmpListe.size());
            this.logger.info(infoMessage);
        } catch (Exception ex) {
            infoMessage = String.format("getElements: Allgemeiner Fehler (%s)", ex.getMessage());
            this.logger.error(infoMessage, ex);
        }
        return tmpListe;
    }

    /**
     * Holt die Dateinamen aller Pdf-Dateien
     * @return List<String> - die Liste aller Pdf-Dateinamen
     */
    public List<String> getPdfNamen_Alt1() {
        List<Element> tmpListe = getElements("datei", nsName);
        List<String> pdfListe = new ArrayList<String>();
        try {
            // Ein Liste kann iteriert werden
            for(Element el: tmpListe) {
                String dateiname = el.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                // Ist es eine Pdf-Datei?
                if (dateiname.toLowerCase().endsWith(".pdf")) {
                    pdfListe.add(dateiname);
                }
            }
            infoMessage = String.format("getPdfNamen: Abschluss mit %d Pdf-Dateinamen.", pdfListe.size());
            this.logger.info(infoMessage);
        } catch (Exception ex) {
            infoMessage = String.format("getPdfNamen: Allgemeiner Fehler (%s)", ex.getMessage());
            this.logger.error(infoMessage, ex);
        } 
        return pdfListe;
    }

    /**
     *  Alternative zu getPdfNamen
     * @return - Liste aller Pdf-Dateinamen als List<String>
     */
    public List<String> getPdfNamen_Alt2() {
        List<Element> elListe = getElements("datei", nsName);
        List<String> pdfListe = new ArrayList<String>();
        try {
            // for-Loop statt Iterator
            for(var i=0;i<elListe.size();i++) {
                if (elListe.get(i).getNodeType() == Node.ELEMENT_NODE) {
                    //  Ist das Element ein Node? Dann dateiname-Element holen
                    String dateiname = ((Element)elListe.get(i)).getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                    if (dateiname.toLowerCase().endsWith(".pdf")) {
                        pdfListe.add(dateiname);
                        infoMessage = String.format("getPdfNamen2: Pdf-Datei: %s", dateiname);
                        this.logger.info(infoMessage);
                    }                
                }
            }
            infoMessage = String.format("getPdfNamen2: Abschluss mit %d Pdf-Dateinamen.", pdfListe.size());
            this.logger.info(infoMessage);
        } catch (Exception ex) {
            infoMessage = String.format("getPdfNamen2: Allgemeiner Fehler (%s)", ex.getMessage());
            this.logger.error(infoMessage, ex);
        }
        return pdfListe;
    }

    /**
     * Extrahiert alle PDF-Dateinamen aus einer XJustiz-XML-Datei
     * @param xmlFilePath Pfad zur XJustiz-XML-Datei
     * @return Liste aller PDF-Dateinamen
     */
    public List<String> getPdfNamen(String xmlFilePath) {
        List<String> pdfFiles = new ArrayList<>();
        
        try {
            // XML-Datei parsen
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Wichtig für XJustiz-XML mit Namespaces
            factory.setNamespaceAware(true); 
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(Paths.get(xmlFilePath).toFile());
            
            // Verzeichnis der XML-Datei ermitteln (PDFs liegen meist im gleichen Verzeichnis)
            Path xmlPath = Paths.get(xmlFilePath);
            Path xmlDirectory = xmlPath.getParent();
            
            // XPath für die Suche nach dateiname-Elementen
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            
            // Suche alle dateiname-Elemente (unabhängig vom Namespace)
            String expression = "//*[local-name()='dateiname']";
            NodeList dateinameNodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            
            // Durchlaufe alle gefundenen dateiname-Elemente
            for (int i = 0; i < dateinameNodes.getLength(); i++) {
                String filename = dateinameNodes.item(i).getTextContent().trim();
                
                // Nur PDF-Dateien hinzufügen
                if (filename.toLowerCase().endsWith(".pdf")) {
                    // Vollständigen Pfad zur PDF-Datei erstellen
                    Path pdfPath = xmlDirectory.resolve(filename);
                    if (Files.exists(pdfPath)) {
                         // Prüfen ob die PDF-Datei gültig/nicht beschädigt ist
                        PdfHelper pdfHelper = new PdfHelper(logger);
                        if (pdfHelper.isPdfFileValid(pdfPath.toFile())) {
                            pdfFiles.add(filename);
                        } else {
                            logger.warn("getPdfNamen: PDF-Datei ist beschädigt oder ungültig: {}", filename);
                        }
                    } else {
                        logger.warn("getPdfNamen: PDF-Datei in XML referenziert, aber nicht gefunden: {}", filename);
                    }
                }
            }
            
        } catch (Exception ex) {
            infoMessage = String.format("getPdfNamen: Allgemeiner Fehler beim Verarbeiten der XML-Datei (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
            ex.printStackTrace();
        }
        
        return pdfFiles;
    }

   
    
    /**
     * Hole alle akte-Element
     * @return - Liste aller Akten als List<Akte>
     */
    public List<Akte> getAkten() {
        // Alle akte-Elemente holen
        List<Element> tmpListe = getElements("akte", nsName);
        List<Akte> aktenListe = new ArrayList<Akte>();
        try {
            for(Element el: tmpListe) {
            String id = el.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
            String aktenTyp = el.getElementsByTagName("code").item(0).getTextContent();
            String anzeigenName = el.getElementsByTagNameNS(nsName,"anzeigename").item(0).getTextContent();
            String zeitpunktErstellung = el.getElementsByTagNameNS(nsName, "erstellungszeitpunktAkteVersand").item(0).getTextContent();
            String az = el.getElementsByTagNameNS(nsName, "aktenzeichen.freitext").item(0).getTextContent();
            Integer nummerImUebergeordnetenContainer = Integer.parseInt(el.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0).getTextContent());
            Akte neuAkte = new Akte(id);
            neuAkte.setAktenTyp(aktenTyp);
            neuAkte.setAnzeigeName(anzeigenName);
            neuAkte.setAktenzeichen(az);
            // Datumsformat z-B. 2021-05-12T09:36:20 auf dd-MM-jjjj umstellen
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date tmpDate = dateFormat.parse(zeitpunktErstellung);
            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                zeitpunktErstellung = dateFormat.format(tmpDate);
            neuAkte.setZeitpunktErstellungVersand(zeitpunktErstellung);
            neuAkte.setNummerImUebergeordnetenContainer(nummerImUebergeordnetenContainer);
            aktenListe.add(neuAkte);
        }
        infoMessage = String.format("getAkten: Abschluss mit %d Akten.", aktenListe.size());
        this.logger.info(infoMessage);
        } catch (Exception ex) {
            infoMessage = String.format("getAkten: Allgemeiner Fehler (%s)", ex.getMessage());
            this.logger.error(infoMessage, ex);
        }
        return aktenListe;
    }

    /**
     * Hole das akte-Element über seine Id per XPath
     * @param Id
     * @return - das akte-Element oder null
     */
    private Element getAkteById(String Id) {
        Element elAkte = null;
        try {
            // Das Akte-Element mit der Id per XPath lokalisieren
            XPath xPathId = XPathFactory.newInstance().newXPath();
            // Namespaceresolver verwenden
            xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
            // XPath-Ausdruck, der das erste akte-Element zum id-Element holt
            // Verwende local-name() anstelle expliziter Namespace-Präfixe
            String xPathExpr = "//*[local-name()='akte']/*[local-name()='identifikation']/*[local-name()='id'][contains(.,'" + Id + "')]/ancestor::*[local-name()='akte'][1]";
            try {
                NodeList nlAkte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
                Node nAkte = nlAkte.item(0);
                elAkte = (Element)nAkte;
            } catch(XPathExpressionException ex) {
                infoMessage = String.format("getAkteById: XPath-Fehler (%s) für %s", ex.getMessage(), xPathExpr);
                logger.error(infoMessage, ex);
            }
        } catch(Exception ex) {
            infoMessage = String.format("getAkteById: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);

        }
        return elAkte;
    }

    /**
     * Hole das teilakte-Element über seine Id per XPath
     * @param Id
     * @return
     */
    public Element getTeilakteById(String Id) {
        Element elTeilakte = null;
        try {
            // Das Akte-Element mit der Id per XPath lokalisieren
            XPath xPathId = XPathFactory.newInstance().newXPath();
            // Namespaceresolver verwenden
            xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
            // XPath-Ausdruck, der das erste teilakte-Element zum id-Element holt
            // Verwende local-name() anstelle expliziter Namespace-Präfixe
            String xPathExpr = "//*[local-name()='teilakte']/*[local-name()='identifikation']/*[local-name()='id'][contains(.,'" + Id + "')]/ancestor::*[local-name()='teilakte'][1]";
            try {
                NodeList nlTeilakte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
                Node nAkte = nlTeilakte.item(0);
                if (nAkte != null && nAkte.getNodeType() == Node.ELEMENT_NODE) {
                    elTeilakte = (Element)nAkte;
                }
            } catch(XPathExpressionException ex) {
                infoMessage = String.format("getTeilakteById: XPath-Fehler (%s) für %s", ex.getMessage(), xPathExpr);
                logger.error(infoMessage, ex);
            }
        } catch(Exception ex) {
            infoMessage = String.format("getTeilakteById: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return elTeilakte;
    }

    /**
     * Gibt an, ob ein akte-Element ein teilakte-Element umfasst
     * @param akte
     * @return - true/false
     */
    private Boolean hasAkteTeilakten(Element akte) {
        Boolean hasTeilakten = false;
        try {
            NodeList teilakten = akte.getElementsByTagNameNS(nsName, "teilakte");
            // Rückgabe ist auch dann nicht null, wenn es kein Element gibt
            hasTeilakten = teilakten.getLength() > 0;
        } catch (Exception ex) {
            infoMessage = String.format("hasAkteTeilakten: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return hasTeilakten;
    }

    /**
     * Holt den Wert des id-Element eines akte-Elements
     * @param akte
     * @return
     */
    private String getAktenId(Element akte) {
        String aktenId = "";
        try {
            aktenId = akte.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
        } catch(Exception ex) {
            infoMessage = String.format("getAktenId: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return aktenId;
    }

    /**
     * Holt alle teilakte-Elemente eines akte-Elements über dessen id
     * @param idAkte
     * @return - Liste von teilakte-Objekten
     */
    public List<Teilakte> getTeilakten(String idAkte) {
        List<Teilakte> teilaktenListe = new ArrayList<Teilakte>();
        try {
            // Alle teilakte-Elemente der aktenId holen
            Element akte = getAkteById(idAkte);
            // Gibt es Teilakten?
            if (hasAkteTeilakten(akte)) {
                try {
                    // Wenn der Typ = DeferredTextImpl ist, dann ist es ein Textknoten
                    // Info: getElementsByTagNameNS gibt ein DeepNodeListImpl-Objekt zurück, das die NodeList-Schnittstelle implementiert
                    NodeList teilakten = akte.getElementsByTagNameNS(nsName, "teilakte");
                    for(int i=0;i<teilakten.getLength();i++) {
                        if (teilakten.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Element elTeilakte = (Element)teilakten.item(i);
                            String idTeilakte = elTeilakte.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
                            Teilakte teilakte = new Teilakte(idTeilakte);
                            String aktenTyp = ((Element)elTeilakte.getElementsByTagNameNS(nsName, "teilaktentyp").item(0)).getElementsByTagName("code").item(0).getTextContent();
                            String anzeigename = elTeilakte.getElementsByTagNameNS(nsName, "anzeigename").item(0).getTextContent();
                            String containerNummer = elTeilakte.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0).getTextContent();
                            int nummerUebergeordneterContainer = Integer.parseInt(containerNummer);
                            teilakte.setAnzeigeName(anzeigename);
                            teilakte.setAktenTyp(aktenTyp);
                            teilakte.setNummerImUebergeordnetenContainer(nummerUebergeordneterContainer);
                            teilaktenListe.add(teilakte);
                            infoMessage = String.format("getTeilakten: Teilakte %s wurde hinzugefügt.", idTeilakte);
                            logger.info(infoMessage);
                        }
                    }
                } catch(Exception ex) {
                    infoMessage = String.format("getTeilakten: Allgemeiner Fehler beim Verarbeiten der Teilakten-Elemente (%s)", ex.getMessage());
                    logger.error(infoMessage, ex);
                }
            }
        } catch (Exception ex) {
            infoMessage = String.format("getTeilakten: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return teilaktenListe;

    }

    /**
     * Holt alle Dokumente eines akte-Elements über dessen Id
     * @param id
     * @param typ
     * @return - Liste aller Dokumente als Dokument-Objekte
     */
    public List<Dokument> getDokumente(String id, Aktentyp typ) {
        List<Dokument> dokumenteListe = new ArrayList<Dokument>();
        try {
            // Welches Tag?
            String tagName = typ == Aktentyp.Akte ? "Akte" : "Teilakte";
            Element akte = null;
            if (typ == Aktentyp.Akte) {
                akte = getAkteById(id);
            } else {
                akte = getTeilakteById(id);
            }
            
            // Check if akte element was found
            if (akte == null) {
                infoMessage = String.format("getDokumente: %s with ID %s not found", tagName, id);
                logger.warn(infoMessage);
                return dokumenteListe;
            }
            
            // Jetzt alle Dokumente holen
            NodeList dokumente = akte.getElementsByTagNameNS(nsName, "dokument");
            for(int i=0; i<dokumente.getLength();i++) {
                if (dokumente.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element elDokument = (Element)dokumente.item(i);
                    
                    // Add null checks for all element accesses
                    Node idNode = elDokument.getElementsByTagNameNS(nsName, "id").item(0);
                    if (idNode == null) {
                        infoMessage = "getDokumente: id element is missing for a document, skipping";
                        logger.warn(infoMessage);
                        continue;
                    }
                    String idDokument = idNode.getTextContent();
                    
                    Node nummerNode = elDokument.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0);
                    if (nummerNode == null) {
                        infoMessage = String.format("getDokumente: nummerImUebergeordnetenContainer is missing for document %s, using default value 0", idDokument);
                        logger.warn(infoMessage);
                        // Set default value if element is missing
                        Dokument dokument = new Dokument(idDokument);
                        dokument.setNummerUebergeordneterContainer(0);
                        dokument.setAnzeigename("Unknown");
                        dokument.setDateiname("Unknown");
                        dokument.setDatumPosteingang("01-01-2025");
                        dokument.setDatumVeraktung("01-01-2025");
                        dokumenteListe.add(dokument);
                        continue;
                    }
                    String nummerUebergeordneterContainer = nummerNode.getTextContent();
                    
                    // Check for posteingangsdatum
                    Node postEingangNode = elDokument.getElementsByTagNameNS(nsName, "posteingangsdatum").item(0);
                    String datumPosteingang = "01-01-2025"; // Default value
                    if (postEingangNode != null) {
                        try {
                            String rawDate = postEingangNode.getTextContent();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                            Date tmpDate = dateFormat.parse(rawDate);
                            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            datumPosteingang = dateFormat.format(tmpDate);
                        } catch (Exception ex) {
                            infoMessage = String.format("getDokumente: Error parsing posteingangsdatum for document %s: %s", idDokument, ex.getMessage());
                            logger.warn(infoMessage);
                        }
                    } else {
                        infoMessage = String.format("getDokumente: posteingangsdatum is missing for document %s, using default value", idDokument);
                        logger.warn(infoMessage);
                    }
                    
                    // Check for veraktungsdatum
                    Node veraktungNode = elDokument.getElementsByTagNameNS(nsName, "veraktungsdatum").item(0);
                    String datumVeraktung = "01-01-2025"; // Default value
                    if (veraktungNode != null) {
                        try {
                            String rawDate = veraktungNode.getTextContent();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date tmpDate = dateFormat.parse(rawDate);
                            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            datumVeraktung = dateFormat.format(tmpDate);
                        } catch (Exception ex) {
                            infoMessage = String.format("getDokumente: Error parsing veraktungsdatum for document %s: %s", idDokument, ex.getMessage());
                            logger.warn(infoMessage);
                        }
                    } else {
                        infoMessage = String.format("getDokumente: veraktungsdatum is missing for document %s, using default value", idDokument);
                        logger.warn(infoMessage);
                    }
                    
                    // Check for anzeigename
                    Node anzeigeNameNode = elDokument.getElementsByTagNameNS(nsName, "anzeigename").item(0);
                    String anzeigeName = "Unknown"; // Default value
                    if (anzeigeNameNode != null) {
                        anzeigeName = anzeigeNameNode.getTextContent();
                    } else {
                        infoMessage = String.format("getDokumente: anzeigename is missing for document %s, using default value", idDokument);
                        logger.warn(infoMessage);
                    }
                    
                    // Check for dateiname
                    Node dateinameNode = elDokument.getElementsByTagNameNS(nsName, "dateiname").item(0);
                    String dateiname = "Unknown.pdf"; // Default value
                    if (dateinameNode != null) {
                        dateiname = dateinameNode.getTextContent();
                    } else {
                        infoMessage = String.format("getDokumente: dateiname is missing for document %s, using default value", idDokument);
                        logger.warn(infoMessage);
                    }
                    
                    Dokument dokument = new Dokument(idDokument);
                    dokument.setNummerUebergeordneterContainer(Integer.parseInt(nummerUebergeordneterContainer));
                    dokument.setAnzeigename(anzeigeName);
                    dokument.setDateiname(dateiname);
                    dokument.setDatumPosteingang(datumPosteingang);
                    dokument.setDatumVeraktung(datumVeraktung);
                    dokumenteListe.add(dokument);
                    infoMessage = String.format("getDokumente: Dokument %s wurde hinzugefügt.", idDokument);
                    logger.info(infoMessage);
                }
            }
        } catch(Exception ex) {
            infoMessage = String.format("getDokumente: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return dokumenteListe;
    }

    /**
     * Verwendet XPath mit local-name() um Dokumente zu finden, unabhängig vom Namespace-Präfix
     * @param id ID der Akte oder Teilakte
     * @param typ Typ (Akte oder Teilakte)
     * @return Liste aller gefundenen Dokumente
     */
    public List<Dokument> getDokumenteWithXPath(String id, Aktentyp typ) {
        List<Dokument> dokumenteListe = new ArrayList<Dokument>();
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceResolver(xDoc));
            
            // XPath-Ausdruck, der alle Dokumente für eine Akte oder Teilakte mit der angegebenen ID findet
            String xPathExpr;
            if (typ == Aktentyp.Akte) {
                xPathExpr = "//*[local-name()='akte']/*[local-name()='identifikation']/*[local-name()='id'][text()='" + id + "']/ancestor::*[local-name()='akte'][1]//*[local-name()='dokument']";
            } else {
                xPathExpr = "//*[local-name()='teilakte']/*[local-name()='identifikation']/*[local-name()='id'][text()='" + id + "']/ancestor::*[local-name()='teilakte'][1]//*[local-name()='dokument']";
            }
            
            NodeList dokumente;
            try {
                dokumente = (NodeList) xpath.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
                infoMessage = String.format("getDokumenteWithXPath: Gefunden %d Dokumente für %s mit ID %s", 
                                        dokumente.getLength(), 
                                        (typ == Aktentyp.Akte ? "Akte" : "Teilakte"), 
                                        id);
                logger.info(infoMessage);
                
                // Verarbeite jedes gefundene Dokument
                for (int i = 0; i < dokumente.getLength(); i++) {
                    Element elDokument = (Element) dokumente.item(i);
                    
                    // ID des Dokuments auslesen
                    String idExpr = ".//*[local-name()='id']";
                    Node idNode = (Node) xpath.evaluate(idExpr, elDokument, XPathConstants.NODE);
                    String idDokument = (idNode != null) ? idNode.getTextContent() : "unknown-" + i;
                    
                    // Container-Nummer auslesen
                    String nummerExpr = ".//*[local-name()='nummerImUebergeordnetenContainer']";
                    Node nummerNode = (Node) xpath.evaluate(nummerExpr, elDokument, XPathConstants.NODE);
                    int nummerUebergeordneterContainer = (nummerNode != null) ? 
                                                    Integer.parseInt(nummerNode.getTextContent()) : 0;
                    
                    // Posteingangsdatum auslesen und formatieren
                    String datumPosteingang = "01-01-2025"; // Standardwert
                    String postEingangExpr = ".//*[local-name()='posteingangsdatum']";
                    Node postEingangNode = (Node) xpath.evaluate(postEingangExpr, elDokument, XPathConstants.NODE);
                    if (postEingangNode != null) {
                        try {
                            String rawDate = postEingangNode.getTextContent();
                            if (rawDate.contains("T")) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                                Date tmpDate = dateFormat.parse(rawDate);
                                dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                datumPosteingang = dateFormat.format(tmpDate);
                            } else {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date tmpDate = dateFormat.parse(rawDate);
                                dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                datumPosteingang = dateFormat.format(tmpDate);
                            }
                        } catch (Exception ex) {
                            infoMessage = String.format("getDokumenteWithXPath: Fehler beim Parsen des Posteingangsdatums für Dokument %s: %s", 
                                                     idDokument, ex.getMessage());
                            logger.warn(infoMessage);
                        }
                    }
                    
                    // Veraktungsdatum auslesen und formatieren
                    String datumVeraktung = "01-01-2025"; // Standardwert
                    String veraktungExpr = ".//*[local-name()='veraktungsdatum']";
                    Node veraktungNode = (Node) xpath.evaluate(veraktungExpr, elDokument, XPathConstants.NODE);
                    if (veraktungNode != null) {
                        try {
                            String rawDate = veraktungNode.getTextContent();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date tmpDate = dateFormat.parse(rawDate);
                            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            datumVeraktung = dateFormat.format(tmpDate);
                        } catch (Exception ex) {
                            infoMessage = String.format("getDokumenteWithXPath: Fehler beim Parsen des Veraktungsdatums für Dokument %s: %s", 
                                                     idDokument, ex.getMessage());
                            logger.warn(infoMessage);
                        }
                    }
                    
                    // Anzeigename auslesen
                    String anzeigeNameExpr = ".//*[local-name()='anzeigename']";
                    Node anzeigeNameNode = (Node) xpath.evaluate(anzeigeNameExpr, elDokument, XPathConstants.NODE);
                    String anzeigeName = (anzeigeNameNode != null) ? anzeigeNameNode.getTextContent() : "Unknown";
                    
                    // Dateiname auslesen
                    String dateinameExpr = ".//*[local-name()='dateiname']";
                    Node dateinameNode = (Node) xpath.evaluate(dateinameExpr, elDokument, XPathConstants.NODE);
                    String dateiname = (dateinameNode != null) ? dateinameNode.getTextContent() : "Unknown.pdf";
                    
                    // Dokument-Objekt erstellen und zur Liste hinzufügen
                    Dokument dokument = new Dokument(idDokument);
                    dokument.setNummerUebergeordneterContainer(nummerUebergeordneterContainer);
                    dokument.setAnzeigename(anzeigeName);
                    dokument.setDateiname(dateiname);
                    dokument.setDatumPosteingang(datumPosteingang);
                    dokument.setDatumVeraktung(datumVeraktung);
                    dokumenteListe.add(dokument);
                    
                    infoMessage = String.format("getDokumenteWithXPath: Dokument %s wurde hinzugefügt.", idDokument);
                    logger.info(infoMessage);
                }
            } catch (XPathExpressionException ex) {
                infoMessage = String.format("getDokumenteWithXPath: XPath-Fehler (%s) für %s", ex.getMessage(), xPathExpr);
                logger.error(infoMessage, ex);
            }
        } catch(Exception ex) {
            infoMessage = String.format("getDokumenteWithXPath: Allgemeiner Fehler (%s)", ex.getMessage());
            logger.error(infoMessage, ex);
        }
        return dokumenteListe;
    }

    public List<String> validateXMLSchema(String xsdPfad, String xmlPfad) throws IOException {
        List<String> errorList = new ArrayList<>();
        try {
            javax.xml.validation.SchemaFactory factory = javax.xml.validation.SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema schema = factory.newSchema(new File(xsdPfad));
            final List<SAXParseException> SAXExceptions = new ArrayList<>();
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException
                {
                    SAXExceptions.add(exception);
                }

                @Override
                public void error(SAXParseException exception) throws SAXException
                {
                    SAXExceptions.add(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException
                {
                    SAXExceptions.add(exception);
                }
            });
            validator.validate(new StreamSource(new File(xmlPfad)));
            // Gab es Fehler?
            if (SAXExceptions.size() > 0) {
                for(SAXParseException ex: SAXExceptions) {
                    String errorMsg = String.format("SAXException in %s: %s", ex.getSystemId(), ex.getMessage());
                    errorList.add(errorMsg);
                }
            }
        } catch(Exception ex) {
            infoMessage = "Allgemeiner Fehler: " + ex.getMessage();
            logger.error(infoMessage, ex);
        }
        return errorList;
    }

}
