package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Element;
import edu.pitt.egm22.biorecipe.Interaction;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class Neo4jCypherConverter extends BioRecipeConverter {
    private static final String CREATE_NODE = "CREATE (n:%node_type% %property_map%)";

    public Neo4jCypherConverter(File inputParentFile) {
        super("neo4j", ".txt", inputParentFile);
    }

    @Override
    public MultivaluedMap<String, String> convertInteractions(List<Interaction> interactions) {
        MultivaluedMap<String, String> output = new MultivaluedHashMap<>();
        Map<String, Element> nodeMap = new HashMap<>();
        Map<String, Interaction> edgeMap = new HashMap<>();
        for (Interaction i : interactions) {
            nodeMap.put(i.getRegulator().getName(), i.getRegulator());
            nodeMap.put(i.getRegulated().getName(), i.getRegulated());
            edgeMap.put(i.getRegulator().getName(), i);
        }

        String fileName = "node-creation";
        for (String node : nodeMap.keySet()) {
            String nodeType = nodeMap.get(node).getType();
            String propertyMap = convertToPropertyMap(nodeMap.get(node));
            String createNodeQuery = CREATE_NODE
                    .replace("%node_type%", nodeType)
                    .replace("%property_map%", propertyMap);
            output.add(fileName, createNodeQuery);
        }

        return output;
    }

    @Override
    public String getSchemaLine(Interaction schema) {
        return null;
    }

    private String convertToPropertyMap(Element e) {
        Field[] fields = FieldUtils.getAllFields(e.getClass());
        StringBuilder result = new StringBuilder("{");
        for (Field f : fields) {
            boolean canAccess = f.canAccess(e);
            f.setAccessible(true);
            try {
                if (result.length() > 1) {
                    result.append(", ");
                }
                String value = (String) f.get(e);
                if (value == null) {
                    value = "";
                }
                result.append(f.getName()).append(": '").append(value).append("'");
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            f.setAccessible(canAccess);
        }
        result.append("}");
        return result.toString();
    }
}
