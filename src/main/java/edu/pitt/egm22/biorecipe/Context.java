package edu.pitt.egm22.biorecipe;

public abstract class Context extends Provenance {
    private String cellLine;
    private String cellType;
    private String tissueType;
    private String organism;

    public String getCellLine() {
        return cellLine;
    }

    public void setCellLine(String cellLine) {
        this.cellLine = cellLine;
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType;
    }

    public String getTissueType() {
        return tissueType;
    }

    public void setTissueType(String tissueType) {
        this.tissueType = tissueType;
    }

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }
}
