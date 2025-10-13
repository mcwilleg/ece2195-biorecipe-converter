package edu.pitt.egm22;

import edu.pitt.egm22.biorecipe.Interaction;
import edu.pitt.egm22.biorecipe.StaticParser;
import edu.pitt.egm22.output.Neo4jCypherConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String defaultInputPath = "D:/University/2025 Fall/ECE2195 Knowledge Graphs/ece2195-gbm-kg/input/original_Q4_LLaMa.csv";

    public static void main(String[] args) {
        String inputPath = defaultInputPath;
        if (args.length >= 1) {
            inputPath = args[0];
        }
        File inputFile = new File(inputPath);
        Interaction schema = null;
        List<Interaction> interactions = new ArrayList<>();
        if (inputFile.isDirectory()) {
            File[] inputFiles = inputFile.listFiles();
            assert inputFiles != null;
            for (File f : inputFiles) {
                if (!f.isDirectory()) {
                    List<Interaction> parsed = parseInteractions(f);
                    schema = parsed.removeFirst();
                    interactions.addAll(parsed);
                }
            }
        } else {
            List<Interaction> parsed = parseInteractions(inputFile);
            schema = parsed.removeFirst();
            interactions.addAll(parsed);
        }
        if (schema == null) {
            System.out.println("No input data found. Exiting...");
            System.exit(1);
        }
        convertToFiles(inputFile.getParentFile(), schema, interactions);
    }

    private static List<Interaction> parseInteractions(File input) {
        try (Scanner fileScanner = new Scanner(input)) {
            return parseInteractions(fileScanner);
        } catch (IOException e) {
            System.out.println("File not found: " + input.getPath());
            System.exit(1);
        }
        return null;
    }

    private static List<Interaction> parseInteractions(Scanner scanner) {
        List<Interaction> interactions = new ArrayList<>();
        while(scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            Interaction interaction = StaticParser.parseInteraction(nextLine);
            interactions.add(interaction);
        }
        return interactions;
    }

    private static void convertToFiles(File inputParentFile, Interaction schema, List<Interaction> interactions) {
        Neo4jCypherConverter converter = new Neo4jCypherConverter(inputParentFile);
        converter.convertAndSave(schema, interactions);
    }
}