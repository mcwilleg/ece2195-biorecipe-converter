package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Neo4jConverter extends BioRecipeConverter {
    public Neo4jConverter(File inputParentFile) {
        super("neo4j", inputParentFile);
    }

    @Override
    public Map<String, List<String>> convertInteractions(File outputDirectory, List<Interaction> interactions) {
        return null;
    }
}
