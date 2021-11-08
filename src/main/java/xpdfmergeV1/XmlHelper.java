/*
file: XmlHelper.java
*/
package xpdfmergeV1;

import java.util.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;

/*
 * Stellt allgemeine Xml-Funktionen für eine bestimmte Xml-Datei
 * zur Verfügung (auf der Basis von javax.xml.parsers)
 * @author Pemo
 */
public class XmlHelper {
    private Log logger = null;
    private String xmlPfad = "";
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
    public XmlHelper(Log logger, String xmlPfad) throws IOException, ParserConfigurationException, SAXException {
        this.logger = logger;
        this.xmlPfad = xmlPfad;
        // Xml-Datei öffnen
        File xmlFile = new File(xmlPfad);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);

        // DocumentBuilder für das Parsen der Xml-Datei anlegen
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        xDoc = dBuilder.parse(xmlFile);
        // Kontrollmeldung
        this.logger.info(String.format("*** XML-Datei %s wurde geparsed ***", xmlPfad));
        this.logger.info("Namespace:" + xDoc.getDocumentElement().getPrefix());
    }

    /**
     *  Holt alle Elemente eines bestimmten Typs
     * @param tagName
     * @param nsName
     * @return List<Element> der Elemente
     */
    public List<Element> getElements(String tagName, String nsName) {
        infoMessage = String.format("*** Aufruf von getElements mit tagName=%s und nsName=%s", tagName, nsName);
        this.logger.info(infoMessage);
        List<Element> tmpList = new ArrayList<Element>();
        Element root = xDoc.getDocumentElement();
        NodeList nlList = root.getElementsByTagNameNS(nsName, tagName);
        // Alle nodes einzeln ansprechen
        for(int i=0; i<nlList.getLength();i++) {
            Node nNode = nlList.item(i);
            // Ist das Element ein Node?
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                tmpList.add((Element)nNode);
            }
        }
        infoMessage = String.format("*** Abschluss von getElements mit %d Elementen.", tmpList.size());
        this.logger.info(infoMessage);
        return tmpList;
    }

    /**
     * Holt die Dateinamen aller Pdf-Dateien
     * @return List<String> - die Liste aller Pdf-Dateinamen
     */
    public List<String> getPdfNamen() {
        List<Element> tmpListe = getElements("datei", nsName);
        List<String> pdfListe = new ArrayList<String>();
        // Ein Liste kann iteriert werden
        for(Element el: tmpListe) {
            String dateiname = el.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
            // Ist es eine Pdf-Datei?
            if (dateiname.toLowerCase().endsWith(".pdf")) {
                pdfListe.add(dateiname);
            }
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
        // for-Loop statt Iterator
        for(var i=0;i<elListe.size();i++) {
            if (elListe.get(i).getNodeType() == Node.ELEMENT_NODE) {
                //  Ist das Element ein Node? Dann dateiname-Element holen
                String dateiname = ((Element)elListe.get(i)).getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                if (dateiname.toLowerCase().endsWith(".pdf")) {
                    pdfListe.add(dateiname);
                    infoMessage = String.format("*** Pdf-Datei: %s", dateiname);
                    this.logger.info(infoMessage);
                }                
            }
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
        for(Element el: tmpListe) {
            String dateiname = el.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
            String id = el.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
            String aktenTyp = el.getElementsByTagName("code").item(0).getTextContent();
            String anzeigenName = el.getElementsByTagNameNS(nsName,"anzeigename").item(0).getTextContent();
            String nummerImUebergeordnetenContainer = el.getElementsByTagNameNS(nsName,"nummerImUebergeordnetenContainer").item(0).getTextContent();
            Akte neuAkte = new Akte(id);
            neuAkte.setAktenTyp(aktenTyp);
            neuAkte.setAnzeigeName(anzeigenName);
            aktenListe.add(neuAkte);
        }
        return aktenListe;
    }

    /**
     * Hole das akte-Element über seine Id per XPath
     * @param Id
     * @return - das akte-Element oder null
     */
    private Element getAkteById(String Id) {
        // Das Akte-Element mit der Id per XPath lokalisieren
        XPath xPathId = XPathFactory.newInstance().newXPath();
        // Namespaceresolver verwenden
        xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
        Element elAkte = null;
        // XPath-Ausdruck, der das erste akte-Element zum id-Element holt
        String xPathExpr = "//ns0:akte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:akte[1]";
        try {
            NodeList nlAkte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
            Node nAkte = nlAkte.item(0);
            elAkte = (Element)nAkte;
        } catch(XPathExpressionException ex) {
            infoMessage = "!!! getAkteById - XPath-Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
            ex.printStackTrace();
        }
        return elAkte;
    }

    /**
     * Hole das teilakte-Element über seine Id per XPath
     * @param Id
     * @return
     */
    public Element getTeilakteById(String Id) {
        // Das Akte-Element mit der Id per XPath lokalisieren
        XPath xPathId = XPathFactory.newInstance().newXPath();
        // Namespaceresolver verwenden
        xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
        Element elTeilakte = null;
        // XPath-Ausdruck, der das erste teilakte-Element zum id-Element holt
        String xPathExpr = "//ns0:teilakte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:teilakte[1]";
        try {
            NodeList nlTeilakte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
            Node nAkte = nlTeilakte.item(0);
            if (nAkte.getNodeType() == Node.ELEMENT_NODE) {
                elTeilakte = (Element)nAkte;
            }
        } catch(XPathExpressionException ex) {
            infoMessage = "!!! getTeilakteById - XPath-Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
            ex.printStackTrace();
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
            infoMessage = "!!! hasAkteTeilakten - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
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
            infoMessage = "!!! getAktenId - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
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
                infoMessage = "!!! getTeilakten - allgemeiner Fehler (" + ex.getMessage() + ")";
                logger.error(infoMessage);
            }
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
        // Welches Tag?
        String tagName = typ == Aktentyp.Akte ? "Akte" : "Teilakte";
        try {
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
            infoMessage = "!!! getDokumente - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
        }

        return dokumenteListe;
    }

}
