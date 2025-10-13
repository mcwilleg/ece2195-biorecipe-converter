package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public abstract class BioRecipeConverter {
    private static final String timestampFormat = "yyyyMMdd-HHmmss";

    private final File inputParentFile;

    protected final String converterType;
    protected final String fileExtension;

    public BioRecipeConverter(String converterType, String fileExtension, File inputParentFile) {
        this.converterType = converterType;
        this.inputParentFile = inputParentFile;
        this.fileExtension = fileExtension;
    }

    public void convertAndSave(Interaction schema, List<Interaction> interactions) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern(timestampFormat));
        String inputParentPath = inputParentFile.getAbsolutePath();
        String outputPath = String.join("_", inputParentPath + '/' + "output", converterType, timestamp);
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
        MultivaluedMap<String, String> converted = convertInteractions(interactions);
        writeToFile(outputPath, schema, converted);
    }

    private void writeToFile(String outputPath, Interaction schema, MultivaluedMap<String, String> converted) {
        for (Map.Entry<String, List<String>> entry : converted.entrySet()) {
            String fileName = cleanFileName(entry.getKey());
            List<String> lines = entry.getValue();
            try (FileWriter writer = new FileWriter(outputPath + '/' + fileName + fileExtension)) {
                if (schema != null) {
                    writer.write(getSchemaLine(schema) + "\n");
                }
                for (String line : lines) {
                    writer.write(line + "\n");
                }
            } catch (IOException e) {
                System.err.println("IO error writing to output files: " + e.getMessage());
                System.out.println("Output files could not be written. Exiting...");
                System.exit(1);
            }
        }
    }

    private static String cleanFileName(String fileName) {
        return URLEncoder.encode(fileName, Charset.defaultCharset());
    }

    public abstract MultivaluedMap<String, String> convertInteractions(List<Interaction> interactions);

    public abstract String getSchemaLine(Interaction schema);
}
