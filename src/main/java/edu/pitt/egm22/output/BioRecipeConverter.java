package edu.pitt.egm22.output;

import edu.pitt.egm22.biorecipe.Interaction;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public abstract class BioRecipeConverter {
    protected final String typeString;
    protected final String fileExtension;

    public BioRecipeConverter(String typeString, String fileExtension) {
        this.typeString = typeString;
        this.fileExtension = fileExtension;
    }

    public final void outputToFile(File outputDirectory, List<Interaction> interactions) throws IOException {
        writeFiles(outputDirectory, interactions);
    }

    protected abstract void writeFiles(File outputDir, List<Interaction> interactions) throws IOException;

    public final String getTypeString() {
        return typeString;
    }

    @SuppressWarnings("unused")
    protected static String cleanFileName(String fileName) {
        return URLEncoder.encode(fileName, Charset.defaultCharset());
    }
}
