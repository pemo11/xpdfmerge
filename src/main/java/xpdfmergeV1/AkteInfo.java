package xpdfmergeV1;

/**
 * Repräsentiert Infos zu einer Akte für die Bookmarks
 * Eventuell ist diese Klasse auch nicht erforderlich, da diese Infos auch über das Akte-Objekt zur Verfügung stehen
 */
public class AkteInfo {
    private int pageCount;
    private String anzeigeName;

    public AkteInfo(String anzeigeName, int pageCount) {
        this.anzeigeName = anzeigeName;
        this.pageCount = pageCount;
    }

    public String getAnzeigeName() {
        return this.anzeigeName;
    }

    public int getPageCount() {
        return this.pageCount;
    }

}
