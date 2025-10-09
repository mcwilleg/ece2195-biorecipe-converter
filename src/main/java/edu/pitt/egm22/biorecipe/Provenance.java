package edu.pitt.egm22.biorecipe;

public abstract class Provenance {
    private String score;
    private String source;
    private String statements;
    private String paperIds;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatements() {
        return statements;
    }

    public void setStatements(String statements) {
        this.statements = statements;
    }

    public String getPaperIds() {
        return paperIds;
    }

    public void setPaperIds(String paperIds) {
        this.paperIds = paperIds;
    }
}
