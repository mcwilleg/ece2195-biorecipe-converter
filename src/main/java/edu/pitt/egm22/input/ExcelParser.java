package edu.pitt.egm22.input;

import edu.pitt.egm22.biorecipe.Element;
import edu.pitt.egm22.biorecipe.Interaction;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExcelParser extends BioRecipeParser {

    @Override
    public List<Interaction> parse(List<File> inputFiles) {
        List<Interaction> output = new ArrayList<>();
        for (File f : inputFiles) {
            try (FileInputStream is = new FileInputStream(f); ReadableWorkbook wb = new ReadableWorkbook(is)) {
                Sheet sheet = wb.getFirstSheet();
                try (Stream<Row> rows = sheet.openStream()) {
                    rows.forEach(r -> output.add(parseInteraction(r, 1)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        output.removeFirst(); // remove the header line
        return output;
    }

    @SuppressWarnings("unused")
    private Interaction parseInteraction(Row r) {
        return parseInteraction(r, 0);
    }

    private Interaction parseInteraction(Row r, int cellOffset) {
        Element regulator = parseElement(r, cellOffset);
        Element regulated = parseElement(r, cellOffset + 8);
        Interaction interaction = new Interaction();
        interaction.setRegulator(regulator);
        interaction.setRegulated(regulated);
        interaction.setSign(r.getCellText(cellOffset + 16));
        interaction.setConnectionType(r.getCellText(cellOffset + 17));
        interaction.setMechanism(r.getCellText(cellOffset + 18));
        interaction.setSite(r.getCellText(cellOffset + 19));
        interaction.setCellLine(r.getCellText(cellOffset + 20));
        interaction.setCellType(r.getCellText(cellOffset + 21));
        interaction.setTissueType(r.getCellText(cellOffset + 22));
        interaction.setOrganism(r.getCellText(cellOffset + 23));
        interaction.setScore(r.getCellText(cellOffset + 24));
        interaction.setSource(r.getCellText(cellOffset + 25));
        interaction.setStatements(r.getCellText(cellOffset + 26));
        interaction.setPaperIds(r.getCellText(cellOffset + 27));
        return interaction;
    }

    private Element parseElement(Row r, int cellOffset) {
        Element element = new Element();
        element.setName(r.getCellText(cellOffset));
        element.setType(r.getCellText(cellOffset + 1));
        element.setSubtype(r.getCellText(cellOffset + 2));
        element.setHgncSymbol(r.getCellText(cellOffset + 3));
        element.setDatabase(r.getCellText(cellOffset + 4));
        element.setId(r.getCellText(cellOffset + 5));
        element.setCompartment(r.getCellText(cellOffset + 6));
        element.setCompartmentId(r.getCellText(cellOffset + 7));
        return element;
    }
}
