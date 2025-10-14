package edu.pitt.egm22.input;

import edu.pitt.egm22.biorecipe.Interaction;

import java.io.File;
import java.util.List;

public abstract class BioRecipeParser {

    public abstract List<Interaction> parse(List<File> inputFiles);
}
