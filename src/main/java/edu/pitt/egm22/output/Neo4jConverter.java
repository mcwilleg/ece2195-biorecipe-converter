package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.File;
import java.util.List;

public class Neo4jConverter extends BioRecipeConverter {
    public Neo4jConverter(File inputParentFile) {
        super("neo4j", inputParentFile);
    }

    @Override
    public MultivaluedMap<String, String> convertInteractions(File outputDirectory, List<Interaction> interactions) {
        MultivaluedMap<String, String> output = new MultivaluedHashMap<>();
        for (Interaction i : interactions) {
            String regulator = i.getRegulator().getName();
            String regulated = i.getRegulated().getName();
            output.add(regulator, regulated);
        }
        return output;
    }

    @Override
    public String getSchemaLine(Interaction schema) {
        return schema.getRegulated().getName();
    }
}
