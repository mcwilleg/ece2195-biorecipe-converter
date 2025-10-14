package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Neo4jCsvConverter extends BioRecipeConverter {
    public Neo4jCsvConverter() {
        super("neo4j", ".csv");
    }

    @Override
    public void writeFiles(File outputDir, List<Interaction> interactions) throws IOException {
        MultivaluedMap<String, String> output = new MultivaluedHashMap<>();
        for (Interaction i : interactions) {
            String regulator = i.getRegulator().getName();
            String regulated = i.getRegulated().getName();
            output.add(regulator, regulated);
        }
        for (Map.Entry<String, List<String>> entry : output.entrySet()) {
            String fileName = entry.getKey();
            File outputFile = outputDir.toPath().resolve(fileName + fileExtension).toFile();
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write("Regulated Name\n");
                writer.write(entry.getValue().getFirst() + "\n");
            }
        }
    }
}
