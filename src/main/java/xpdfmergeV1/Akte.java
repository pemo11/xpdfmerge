package xpdfmergeV1;

/**
 * Repräsentiert ein Akte-Element
 * @author Pemo
 */
public class Akte {
    private String id;
    private Integer nummerImUebergeordnetenContainer;
    private String aktenTyp;
    private String anzeigeName;
    private String aktenzeichen;
    private String zeitpunktErstellungVersand;
    boolean hasTeilakten;

    public Akte(String id) {
        this.id = id;
    }

    public Integer getNummerImUebergeordnetenContainer() {
        return nummerImUebergeordnetenContainer;
    }

    public void setNummerImUebergeordnetenContainer(Integer nummerImUebergeordnetenContainer) {
        this.nummerImUebergeordnetenContainer = nummerImUebergeordnetenContainer;
    }

    public void setZeitpunktErstellungVersand(String zeitpunkt) { this.zeitpunktErstellungVersand = zeitpunkt;  }

    public String getZeitpunktErstellungVersand() {  return this.zeitpunktErstellungVersand;  }

    public String getAktenzeichen() {  return this.aktenzeichen;  }

    public void setAktenzeichen(String az) { this.aktenzeichen = az; }

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
