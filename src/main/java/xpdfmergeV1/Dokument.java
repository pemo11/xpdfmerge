package xpdfmergeV1;

/**
 * Repräsentiert ein Akte-Element
 * @author Pemo
 */
public class Dokument {
    private String id;
    private String anzeigename;
    private String dateiname;
    private String datumPosteingang;
    private String datumVeraktung;
    private Integer nummerUebergeordneterContainer;

    public Dokument(String Id) {
        this.id = Id;
    }

    public String getId() {
        return id;
    }

    public String getAnzeigename() {
        return anzeigename;
    }

    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    public String getDatumPosteingang() {
        return datumPosteingang;
    }

    public void setDatumPosteingang(String datum) {
        this.datumPosteingang = datum;
    }

    public String getDatumVeraktung() {
        return datumVeraktung;
    }

    public void setDatumVeraktung(String datum) {
        this.datumVeraktung = datum;
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
