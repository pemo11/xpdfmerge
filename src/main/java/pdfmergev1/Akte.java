package pdfmergev1;

/**
 * Repräsentiert ein Akte-Element
 * @author Pemo
 */
public class Akte {
    private String id;
    private String anzeigeName;
    private String aktenTyp;
    boolean hasTeilakten;

    public Akte(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getAnzeigeName() {
        return anzeigeName;
    }

    public void setAnzeigeName(String anzeigeName) {
        this.anzeigeName = anzeigeName;
    }

    public String getAktenTyp() {
        return aktenTyp;
    }

    public void setAktenTyp(String aktenTyp) {
        this.aktenTyp = aktenTyp;
    }

    public boolean getHasTeilakten() {
        return hasTeilakten;
    }

    public void setHasTeilakten(boolean teilakte) {
        hasTeilakten = teilakte;
    }

}
