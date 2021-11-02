/*
file: XmlHelper.java
*/
package pdfmergev1;

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
import org.apache.commons.logging.LogFactory;

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


    public XmlHelper(Log logger, String xmlPfad) throws IOException, ParserConfigurationException, SAXException {
        this.logger = logger;
        this.xmlPfad = xmlPfad;
        // Xml-Datei öffnen
        File xmlFile = new File(xmlPfad);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);

        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        xDoc = dBuilder.parse(xmlFile);
        this.logger.info(String.format("*** XML-Datei %s wurde geparsed ***", xmlPfad));
        this.logger.info("Namespace:" + xDoc.getDocumentElement().getPrefix());

    }

    /*
        Holt alle Elemente eines bestimmten Typs
     */
    public List<Element> getElements(String tagName, String nsName) {
        this.logger.info(String.format("*** Aufruf von getElements mit tagName=%s und nsName=%s", tagName, nsName));
        List<Element> tmpList = new ArrayList<Element>();
        Element root = xDoc.getDocumentElement();
        NodeList nlList = root.getElementsByTagNameNS(nsName, tagName);
        for(int i=0; i<nlList.getLength();i++) {
            Node nNode = nlList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                tmpList.add((Element)nNode);
            }
        }
        return tmpList;
    }

    /*
        Holt die Dateinamen aller Pdf-Dateien
     */
    public List<String> getPdfNamen() {
        List<Element> tmpListe = getElements("datei", nsName);
        List<String> pdfListe = new ArrayList<String>();
        for(Element el: tmpListe) {
            String dateiname = el.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
            // Ist es eine Pdf-Datei?
            if (dateiname.toLowerCase().endsWith(".pdf")) {
                pdfListe.add(dateiname);
            }
        }
        return pdfListe;
    }

    // Alternative zu getPdfNamen
    public List<String> getPdfNamen2() {
        List<Element> elListe = getElements("datei", nsName);
        List<String> pdfListe = new ArrayList<String>();
        for(var i=0;i<elListe.size();i++) {
            if (elListe.get(i).getNodeType() == Node.ELEMENT_NODE) {
                String dateiname = ((Element)elListe.get(i)).getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                if (dateiname.toLowerCase().endsWith(".pdf")) {
                    pdfListe.add(dateiname);
                    String infoMessage = String.format("Pdf-Datei: %s", dateiname);
                    this.logger.info(infoMessage);
                }                
            }
        }
        return pdfListe;
    }

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
            // Element elxfachspezDaten = (Element)el.getElementsByTagNameNS(nsName, "xjustiz.fachspezifischeDaten").item(0);
            // Element elAktentyp = (Element)elxfachspezDaten.getElementsByTagNameNS(nsName, "aktentyp").item(0);
            // String aktenTyp = ((Element)elAktentyp.getElementsByTagNameNS(nsName, "code").item(0)).getTextContent();
            neuAkte.setAktenTyp(aktenTyp);
            neuAkte.setAnzeigeName(anzeigenName);
            aktenListe.add(neuAkte);
        }
        return aktenListe;
    }

    private Element getAkteById(String Id) {
        // Das Akte-Element mit der Id per XPath lokalisieren
        XPath xPathId = XPathFactory.newInstance().newXPath();
        xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
        Element elAkte = null;
        String xPathExpr = "//ns0:akte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:akte[1]";
        try {
            NodeList nlAkte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
            Node nAkte = nlAkte.item(0);
            elAkte = (Element)nAkte;
        } catch(XPathExpressionException ex) {
            ex.printStackTrace();
        }
        return elAkte;
    }

    public Element getTeilakteById(String Id) {
        // Das Akte-Element mit der Id per XPath lokalisieren
        XPath xPathId = XPathFactory.newInstance().newXPath();
        xPathId.setNamespaceContext(new NamespaceResolver(xDoc));
        Element elTeilakte = null;
        Node nAkte = null;
        String xPathExpr = "//ns0:teilakte/ns0:identifikation/ns0:id[contains(.,'" + Id + "')]/ancestor::ns0:teilakte[1]";
        try {
            NodeList nlTeilakte = (NodeList)xPathId.evaluate(xPathExpr, xDoc, XPathConstants.NODESET);
            nAkte = nlTeilakte.item(0);
            if (nAkte.getNodeType() == Node.ELEMENT_NODE) {
                elTeilakte = (Element)nAkte;
            }
        } catch(XPathExpressionException ex) {
            ex.printStackTrace();
        }
        return elTeilakte;
    }

    private Boolean hasAkteTeilakten(Element akte) {
        Boolean hasTeilakten = false;
        try {
            NodeList teilakten = akte.getElementsByTagNameNS(nsName, "teilakte");
            hasTeilakten = teilakten != null;
        } catch (Exception ex) {
            infoMessage = "hasAkteTeilakten - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
        }
        return hasTeilakten;
    }

    private String getAktenId(Element akte) {
        String aktenId = "";
        try {
            aktenId = akte.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
        } catch(Exception ex) {
            infoMessage = "getAktenId - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
        }
        return aktenId;
    }

    public List<Teilakte> getTeilakten(String idAkte) {
        List<Teilakte> teilaktenListe = new ArrayList<Teilakte>();
        // Alle teilakte-Elemente der aktenId holen
        Element akte = getAkteById(idAkte);
        // Gibt es Teilakten?
        if (hasAkteTeilakten(akte)) {
            try {
                NodeList teilakten = akte.getElementsByTagNameNS(nsName, "teilakte");
                for(int i=0;i<teilakten.getLength();i++) {
                    Node nNode = teilakten.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element elTeilakte = (Element)nNode;
                        String idTeilakte = elTeilakte.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
                        Teilakte teilakte = new Teilakte(idTeilakte);
                        // TODO: Weitere Attribute hinzufügen
                        teilaktenListe.add(teilakte);
                    }
                }
            } catch(Exception ex) {
                infoMessage = "getTeilakten - allgemeiner Fehler (" + ex.getMessage() + ")";
                logger.error(infoMessage);
            }
        }
        return teilaktenListe;

    }


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
                Node nNode = dokumente.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element elDokument = (Element)nNode;
                    String idTeilakte = elDokument.getElementsByTagNameNS(nsName, "id").item(0).getTextContent();
                    String nummerUebergeordneterContainer = elDokument.getElementsByTagNameNS(nsName, "nummerImUebergeordnetenContainer").item(0).getTextContent();
                    String posteingang = elDokument.getElementsByTagNameNS(nsName, "posteingangsdatum").item(0).getTextContent();
                    String anzeigeName = elDokument.getElementsByTagNameNS(nsName, "anzeigename").item(0).getTextContent();
                    String dateiname = elDokument.getElementsByTagNameNS(nsName, "dateiname").item(0).getTextContent();
                    Dokument dokument = new Dokument(idTeilakte);
                    dokument.setNummerUebergeordneterContainer(Integer.parseInt(nummerUebergeordneterContainer));
                    dokument.setAnzeigename(anzeigeName);
                    dokument.setDateiname(dateiname);
                    dokument.setPosteingangsDatum(posteingang);
                    dokumenteListe.add(dokument);
                }
            }
        } catch(Exception ex) {
            infoMessage = "getDokumente - allgemeiner Fehler (" + ex.getMessage() + ")";
            logger.error(infoMessage);
        }

        return dokumenteListe;
    }

}
