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
    public List<String> getPdfNamen() {
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
    public List<String> getPdfNamen2() {
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
            String posteingangsDatum = el.getElementsByTagNameNS(nsName, "posteingangsdatum").item(0).getTextContent();
            Integer nummerImUebergeordnetenContainer = Integer.parseInt(el.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0).getTextContent());
            Akte neuAkte = new Akte(id);
            neuAkte.setAktenTyp(aktenTyp);
            neuAkte.setAnzeigeName(anzeigenName);
            // Datumsformat z-B. 2021-05-12T09:36:20 auf dd-MM-jjjj umstellen
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date tmpDate = dateFormat.parse(posteingangsDatum);
            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            posteingangsDatum = dateFormat.format(tmpDate);
            neuAkte.setPosteingangsDatum(posteingangsDatum);
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
            String xPathExpr = "//ns0:akte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:akte[1]";
            try {
                NodeList nlAkte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
                Node nAkte = nlAkte.item(0);
                elAkte = (Element)nAkte;
            } catch(XPathExpressionException ex) {
                infoMessage = String.format("getAkteById: XPath-Fehler (%s)", ex.getMessage());
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
            String xPathExpr = "//ns0:teilakte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:teilakte[1]";
            try {
                NodeList nlTeilakte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
                Node nAkte = nlTeilakte.item(0);
                if (nAkte.getNodeType() == Node.ELEMENT_NODE) {
                    elTeilakte = (Element)nAkte;
                }
            } catch(XPathExpressionException ex) {
                infoMessage = String.format("getTeilakteById: XPath-Fehler (%s)", ex.getMessage());
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
            // Jetzt alle Dokumente holen
            NodeList dokumente = akte.getElementsByTagNameNS(nsName, "dokument");
            for(int i=0; i<dokumente.getLength();i++) {
                if (dokumente.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element elDokument = (Element)dokumente.item(i);
                    String idDokument = elDokument.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
                    String nummerUebergeordneterContainer = elDokument.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0).getTextContent();
                    String posteingang = elDokument.getElementsByTagNameNS(nsName, "posteingangsdatum").item(0).getTextContent();
                    String anzeigeName = elDokument.getElementsByTagNameNS(nsName, "anzeigename").item(0).getTextContent();
                    String dateiname = elDokument.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                    Dokument dokument = new Dokument(idDokument);
                    dokument.setNummerUebergeordneterContainer(Integer.parseInt(nummerUebergeordneterContainer));
                    dokument.setAnzeigename(anzeigeName);
                    dokument.setDateiname(dateiname);
                    dokument.setPosteingangsDatum(posteingang);
                    dokumenteListe.add(dokument);
                }
            }
        } catch(Exception ex) {
            infoMessage = String.format("getDokumente: Allgemeiner Fehler (%s)", ex.getMessage());
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
