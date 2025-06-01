/**
 * File:  NamespaceResolver.java
 * @author: pemo
 */

package xpdfmergeV1;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

public class NamespaceResolver implements NamespaceContext
{
    private final Document sourceDocument;
    private final String fixedPrefix = "ns";  // Wir verwenden immer "ns" im XPath

    public NamespaceResolver(Document document) {
        this.sourceDocument = document;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (fixedPrefix.equals(prefix)) {
            // Liefert den Namespace-URI des Root-Elements, z. B. "http://www.xjustiz.de"
            return sourceDocument.getDocumentElement().getNamespaceURI();
        }
        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return fixedPrefix;
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return null;
    }
}
