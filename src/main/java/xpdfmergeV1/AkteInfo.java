package xpdfmergeV1;

/**
 * Repräsentiert Infos zu einer Akte für die Bookmarks
 * Eventuell ist diese Klasse auch nicht erforderlich, da diese Infos auch über das Akte-Objekt zur Verfügung stehen
 */
public class AkteInfo {
    private int pageNumber;
    private String anzeigeName;

    public AkteInfo(String anzeigeName, int pageNumber) {
        this.anzeigeName = anzeigeName;
        this.pageNumber = pageNumber;
    }

    public String getAnzeigeName() {
        return this.anzeigeName;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

}
