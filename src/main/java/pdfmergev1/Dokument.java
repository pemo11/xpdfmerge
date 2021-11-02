package pdfmergev1;

/**
 * Repräsentiert ein Akte-Element
 * @author Pemo
 */
public class Dokument {
    private String id;
    private String anzeigename;
    private String dateiname;
    private String posteingangsDatum;
    private Integer nummerUebergeordneterContainer;

    public Dokument(String Id) {
        this.id = Id;
    }

    public String getAnzeigename() {
        return anzeigename;
    }

    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    public String getPosteingangsDatum() {
        return posteingangsDatum;
    }

    public void setPosteingangsDatum(String posteingangsDatum) {
        this.posteingangsDatum = posteingangsDatum;
    }

    public String getDateiname() {
        return this.dateiname;
    }

    public void setDateiname(String dateiname) {
        this.dateiname = dateiname;
    }

    public Integer getNummerUebergeordneterContainer() {
        return nummerUebergeordneterContainer;
    }

    public void setNummerUebergeordneterContainer(Integer nummerUebergeordneterContainer) {
        this.nummerUebergeordneterContainer = nummerUebergeordneterContainer;
    }


}
