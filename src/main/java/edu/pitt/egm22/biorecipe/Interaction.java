package edu.pitt.egm22.biorecipe;

public class Interaction extends Context {
    private Element regulator;
    private Element regulated;
    private String sign;
    private String connectionType;
    private String mechanism;
    private String site;

    public Element getRegulator() {
        return regulator;
    }

    public void setRegulator(Element regulator) {
        this.regulator = regulator;
    }

    public Element getRegulated() {
        return regulated;
    }

    public void setRegulated(Element regulated) {
        this.regulated = regulated;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
