package edu.pitt.egm22.biorecipe;

public class Element extends Provenance {
    private String name;
    private String type;
    private String subtype;
    private String hgncSymbol;
    private String database;
    private String id;
    private String compartment;
    private String compartmentId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getHgncSymbol() {
        return hgncSymbol;
    }

    public void setHgncSymbol(String hgncSymbol) {
        this.hgncSymbol = hgncSymbol;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompartment() {
        return compartment;
    }

    public void setCompartment(String compartment) {
        this.compartment = compartment;
    }

    public String getCompartmentId() {
        return compartmentId;
    }

    public void setCompartmentId(String compartmentId) {
        this.compartmentId = compartmentId;
    }
}
