package edu.pitt.egm22;

import edu.pitt.egm22.biorecipe.Interaction;
import edu.pitt.egm22.biorecipe.Parser;
import edu.pitt.egm22.output.Neo4jConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String defaultInputPath = """
    D:\\University\\2025 Fall\\ECE2195 Knowledge Graphs\\ece2195-gbm-kg\\original_Q2_LLaMa.csv
    """;

    public static void main(String[] args) {
        String inputPath = defaultInputPath;
        if (args.length >= 1) {
            inputPath = args[0];
        }
        File inputFile = new File(inputPath);
        List<Interaction> interactions = new ArrayList<>();
        if (inputFile.isDirectory()) {
            File[] inputFiles = inputFile.listFiles();
            assert inputFiles != null;
            for (File f : inputFiles) {
                if (!f.isDirectory()) {
                    interactions.addAll(parseInteractions(f));
                }
            }
        } else {
            interactions.addAll(parseInteractions(inputFile));
        }
        convertToFiles(inputFile.getParentFile(), interactions);
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
            Interaction interaction = Parser.parseInteraction(nextLine);
            interactions.add(interaction);
        }
        return interactions;
    }

    private static void convertToFiles(File inputParentFile, List<Interaction> interactions) {
        Neo4jConverter converter = new Neo4jConverter(inputParentFile);
        converter.convertAndSave(interactions);
    }
}