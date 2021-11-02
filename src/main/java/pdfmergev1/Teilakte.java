package pdfmergev1;

/**
 * Repräsentiert ein Teilakte-Element
 * @author Pemo
 */

public class Teilakte {
    private String id;
    private String anzeigeName;
    private String aktenTyp;
    private Integer nummerImUebergeordnetenContainer;

    public Integer getNummerImUebergeordnetenContainer() {
        return this.nummerImUebergeordnetenContainer;
    }

    public void setNummerImUebergeordnetenContainer(Integer nummer) {
        this.nummerImUebergeordnetenContainer = nummer;
    }

    public Teilakte(String id) {
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

}
