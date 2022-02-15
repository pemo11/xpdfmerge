package xpdfmergeV1;

/**
 * Repräsentiert ein Akte-Element
 * @author Pemo
 */
public class Akte {
    private String id;
    private String anzeigeName;
    private String veraktungsDatum;
    private String posteingangsDatum;
    private String aktenTyp;
    private Integer nummerImUebergeordnetenContainer;
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

    public void setVeraktungsdatum(String datum) { this.veraktungsDatum = datum;  }

    public String getVeraktungsDatum() {  return this.veraktungsDatum;  }

    public String getPosteingangsDatum() {  return this.posteingangsDatum;  }

    public void setPosteingangsDatum(String datum) { this.posteingangsDatum = datum;  }

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
