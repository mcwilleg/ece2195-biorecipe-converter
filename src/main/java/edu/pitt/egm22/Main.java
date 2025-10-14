package edu.pitt.egm22;

import edu.pitt.egm22.biorecipe.Interaction;
import edu.pitt.egm22.input.BioRecipeParser;
import edu.pitt.egm22.input.ExcelParser;
import edu.pitt.egm22.output.BioRecipeConverter;
import edu.pitt.egm22.output.Neo4jCypherConverter;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String timestampFormat = "yyyyMMdd-HHmmss";

    private static final String defaultInputPath = "D:/University/2025 Fall/ECE2195 Knowledge Graphs/ece2195-gbm-kg/input/Q4_LLaMa.xlsx";

    public static void main(String[] args) {
        String inputPath = defaultInputPath;
        if (args.length >= 1) {
            inputPath = args[0];
        }
        File inputSource = new File(inputPath);
        List<File> inputFiles = getInputFiles(inputSource);
        BioRecipeParser parser = new ExcelParser();
        List<Interaction> interactions = parser.parse(inputFiles);
        if (interactions.isEmpty()) {
            System.out.println("No input data found. Exiting...");
            System.exit(1);
        }
        BioRecipeConverter converter = new Neo4jCypherConverter();
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern(timestampFormat));
        String inputParentPath = inputSource.getParent();
        String typeString = converter.getTypeString();
        String outputPath = String.join("_", inputParentPath + '/' + "output", typeString, timestamp);
        try {
            File outputDirectory = new File(outputPath);
            if(!outputDirectory.mkdirs()) {
                throw new IOException("failed to create necessary output directories");
            }
            converter.outputToFile(outputDirectory, interactions);
        } catch (IOException e) {
            System.err.println("IO error writing to output files: " + e.getMessage());
            System.exit(1);
        }
    }

    private static List<File> getInputFiles(File inputSource) {
        List<File> inputFiles = new ArrayList<>();
        if (inputSource.isDirectory()) {
            File[] inputSourceChildren = inputSource.listFiles();
            assert inputSourceChildren != null;
            for (File f : inputSourceChildren) {
                if (!f.isDirectory()) {
                    inputFiles.add(f);
                }
            }
        } else {
            inputFiles.add(inputSource);
        }
        return inputFiles;
    }
}