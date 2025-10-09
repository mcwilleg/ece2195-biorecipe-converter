package edu.pitt.egm22.biorecipe;

import java.util.Arrays;

public class Parser {
    public static Interaction parseInteraction(String line) {
        String[] split = line.split(",");
        Element regulator = parseElement(Arrays.copyOfRange(split, 0, 8));
        Element regulated = parseElement(Arrays.copyOfRange(split, 8, 16));
        Interaction interaction = new Interaction();
        interaction.setRegulator(regulator);
        interaction.setRegulated(regulated);
        interaction.setSign(split[16]);
        interaction.setConnectionType(split[17]);
        interaction.setMechanism(split[18]);
        interaction.setSite(split[19]);
        populateContext(interaction, Arrays.copyOfRange(split, 20, 24));
        populateProvenance(interaction, Arrays.copyOfRange(split, 24, 28));
        return interaction;
    }

    private static Element parseElement(String[] elementValues) {
        Element element = new Element();
        element.setName(elementValues[0]);
        element.setType(elementValues[1]);
        element.setSubtype(elementValues[2]);
        element.setHgncSymbol(elementValues[3]);
        element.setDatabase(elementValues[4]);
        element.setId(elementValues[5]);
        element.setCompartment(elementValues[6]);
        element.setCompartmentId(elementValues[7]);
        return element;
    }

    private static void populateContext(Context context, String[] contextValues) {
        context.setCellLine(contextValues[0]);
        context.setCellType(contextValues[1]);
        context.setTissueType(contextValues[2]);
        context.setOrganism(contextValues[3]);
    }

    private static void populateProvenance(Provenance provenance, String[] provenanceValues) {
        provenance.setScore(provenanceValues[0]);
        provenance.setSource(provenanceValues[1]);
        provenance.setStatements(provenanceValues[2]);
        provenance.setPaperIds(provenanceValues[3]);
    }
}
