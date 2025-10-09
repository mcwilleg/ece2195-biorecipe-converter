package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public abstract class BioRecipeConverter {
    private static final String timestampFormat = "yyyyMMdd-HHmmss";

    private final File inputParentFile;

    protected final String converterType;

    public BioRecipeConverter(String converterType, File inputParentFile) {
        this.converterType = converterType;
        this.inputParentFile = inputParentFile;
    }

    public void convertAndSave(List<Interaction> interactions) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern(timestampFormat));
        String inputParentPath = inputParentFile.getAbsolutePath();
        String outputPath = String.join("_", inputParentPath + File.separatorChar + "output", converterType, timestamp);
        File outputDirectory = new File(outputPath);
        if (outputDirectory.exists()) {
            System.out.print("Output directory already exists! Overwrite? Y/N: ");
            Scanner console = new Scanner(System.in);
            String overwrite = console.nextLine();
            if (List.of("yes", "y").contains(StringUtils.toRootLowerCase(overwrite))) {
                try {
                    FileUtils.deleteDirectory(outputDirectory);
                } catch (IOException e) {
                    System.err.println("IO error deleting existing output directory: " + e.getMessage());
                    System.out.println("Output directory could not be deleted, please delete manually. Exiting...");
                    System.exit(1);
                }
            } else {
                System.out.println("Output directory already exists, cannot overwrite. Exiting...");
                System.exit(1);
            }
        } else if (!outputDirectory.mkdirs()) {
            System.out.println("Output directory could not be created. Exiting...");
            System.exit(1);
        }
        Map<String, List<String>> converted = convertInteractions(outputDirectory, interactions);
        writeToFile(outputPath, converted);
    }

    private void writeToFile(String outputPath, Map<String, List<String>> converted) {
        for (Map.Entry<String, List<String>> entry : converted.entrySet()) {
            String fileName = entry.getKey();
            List<String> lines = entry.getValue();
            try (FileWriter writer = new FileWriter(outputPath + File.separatorChar + fileName)) {
                for (String line : lines) {
                    writer.write(line);
                }
            } catch (IOException e) {
                System.err.println("IO error writing to output files: " + e.getMessage());
                System.out.println("Output files could not be written. Exiting...");
                System.exit(1);
            }
        }
    }

    public abstract Map<String, List<String>> convertInteractions(File outputDirectory, List<Interaction> interactions);
}
