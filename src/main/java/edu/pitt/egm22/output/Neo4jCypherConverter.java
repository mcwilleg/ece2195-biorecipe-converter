package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Element;
import edu.pitt.egm22.biorecipe.Interaction;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.text.WordUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Neo4jCypherConverter extends BioRecipeConverter {
    private static final String CREATE_NODE = "CREATE (%variable%:%node_type% %property_map%)";
    private static final String EDGE_NODE = "CREATE (%head%)-[:REGULATES %property_map%]->(%tail%)";

    public Neo4jCypherConverter() {
        super("neo4j", ".txt");
    }

    @Override
    public void writeFiles(File outputDir, List<Interaction> interactions) throws IOException {
        Map<String, Element> nodeMap = new HashMap<>();
        Map<String, Interaction> edgeMap = new HashMap<>();
        Map<String, String> nodeVariableMap = new HashMap<>();
        for (Interaction i : interactions) {
            boolean regulatorValid = isValidNode(i.getRegulator());
            boolean regulatedValid = isValidNode(i.getRegulated());
            if (regulatorValid) {
                nodeMap.put(i.getRegulator().getName(), i.getRegulator());
            }
            if (regulatedValid) {
                nodeMap.put(i.getRegulated().getName(), i.getRegulated());
            }
            if (regulatorValid && regulatedValid) {
                edgeMap.put(i.getRegulator().getName(), i);
            }
        }

        String fileName = "graph-creation";
        File outputFile = outputDir.toPath().resolve(fileName + fileExtension).toFile();
        RandomStringGenerator random = RandomStringGenerator.builder().withinRange('a', 'z').get();
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String node : nodeMap.keySet()) {
                String nodeType = nodeMap.get(node).getType();
                String propertyMap = convertToNodePropertyMap(nodeMap.get(node));
                String variableName = random.generate(8);
                nodeVariableMap.put(node, variableName);
                String createNodeQuery = CREATE_NODE
                        .replace("%variable%", variableName)
                        .replace("%node_type%", cleanNodeType(nodeType))
                        .replace("%property_map%", propertyMap);
                writer.write(createNodeQuery + "\n");
            }
            writer.write("\n");
            for (String headNode : edgeMap.keySet()) {
                Interaction i = edgeMap.get(headNode);
                String tailNode = i.getRegulated().getName();
                String propertyMap = convertToEdgePropertyMap(i);
                String createNodeQuery = EDGE_NODE
                        .replace("%head%", nodeVariableMap.get(headNode))
                        .replace("%tail%", nodeVariableMap.get(tailNode))
                        .replace("%property_map%", propertyMap);
                writer.write(createNodeQuery + "\n");
            }
        }
    }

    private boolean isValidNode(Element e) {
        return !e.getName().isBlank();
    }

    private String cleanNodeType(String nodeTypeRaw) {
        return WordUtils.capitalize(nodeTypeRaw).replaceAll("[- ]", "");
    }

    private String convertToNodePropertyMap(Element e) {
        return "{" +
                "name: '" + e.getName() + "', " +
                "type: '" + e.getType() + "', " +
                "subtype: '" + e.getSubtype() + "', " +
                "hgncSymbol: '" + e.getHgncSymbol() + "', " +
                "database: '" + e.getDatabase() + "', " +
                "id: '" + e.getId() + "', " +
                "compartment: '" + e.getCompartment() + "', " +
                "compartmentId: '" + e.getCompartmentId() + "'" +
                "}";
    }

    private String convertToEdgePropertyMap(Interaction i) {
        return "{" +
                "sign: '" + i.getSign() + "', " +
                "connectionType: '" + i.getConnectionType() + "', " +
                "mechanism: '" + i.getMechanism() + "', " +
                "site: '" + i.getSite() + "', " +
                "cellLine: '" + i.getCellLine() + "', " +
                "cellType: '" + i.getCellType() + "', " +
                "tissueType: '" + i.getTissueType() + "', " +
//                "statements: '" + i.getStatements() + "', " +
                "paperIds: '" + i.getPaperIds() + "'" +
                "}";
    }
}
