/**
 * Copyright French Prime minister Office/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@programmevitam.fr
 * <p>
 * This software is developed as a validation helper tool, for constructing Submission Information Packages (archives
 * sets) in the Vitam program whose purpose is to implement a digital archiving back-office system managing high
 * volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA archiveTransfer the following URL "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.tools.sedalib.inout.importer;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import fr.gouv.vitam.tools.sedalib.core.ArchiveUnit;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.Content;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibProgressLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * The Class CSVTreeToDataObjectPackageImporter.
 * <p>
 * Class for CSV Holding Tree description import in
 * DataObjectPackage object.
 * <p>
 * The csv has one header line and then the csv file line format is:
 * <ul>
 * <li>id : uniq number</li>
 * <li>nom : ArchiveUnit "title" metadata</li>
 * <li>observations : ArchiveUnit "description"</li>
 * <li>cote : specific suffix for this ArchiveUnit in the hierarchic cotation id</li>
 * <li>série : "mother" ArchiveUnit cotation id</li>
 * <li>... : any other not used content</li>
 * </ul>
 * Lines without "série" field are supposed to represent an ArchiveUnit at description level
 * "Series" others are at "Subseries" description level.
 */
public class CSVTreeToDataObjectPackageImporter {

    /**
     * The csv file.
     */
    private String csvFileName;

    /**
     * The encoding format.
     */
    private String encoding;

    /**
     * The separator char.
     */
    private char separator;

    /**
     * The data object package.
     */
    private DataObjectPackage dataObjectPackage;

    /**
     * The lines map.
     */
    private Map<String, List<Line>> linesMap;

    /**
     * Utility class for csv line encoding.
     */
    private class Line {
        String uniqId;
        String title;
        String description;
        String suffix;
        String motherId;

        protected Line(String uniqId, String title, String description, String suffix, String motherId) {
            this.uniqId = uniqId;
            this.title = title;
            this.description = description;
            this.suffix = suffix;
            this.motherId = motherId;
        }
    }

    /**
     * The end.
     */
    Instant start, end;

    /**
     * The progress logger.
     */
    private SEDALibProgressLogger sedaLibProgressLogger;

    /**
     * Instantiates a new csv tree file importer.
     *
     * @param csvFileName           the csv file name
     * @param encoding              the encoding format string (most of the time UTF8 or Cp1252)
     * @param separator             the char used as column separator (; or , or \t...)
     * @param sedaLibProgressLogger the progress logger or null if no progress log expected
     * @throws SEDALibException if file doesn't exist
     */
    public CSVTreeToDataObjectPackageImporter(String csvFileName, String encoding, char separator, SEDALibProgressLogger sedaLibProgressLogger) throws SEDALibException {
        Path pathFile;

        pathFile = Paths.get(csvFileName);
        if (!Files.isRegularFile(pathFile, java.nio.file.LinkOption.NOFOLLOW_LINKS))
            throw new SEDALibException("Le chemin [" + csvFileName + "] pointant vers le fichier csv ne désigne pas un fichier");

        this.csvFileName = csvFileName;
        this.sedaLibProgressLogger = sedaLibProgressLogger;
        this.encoding = encoding;
        this.separator = separator;
    }

    /**
     * Read csv file and construct the map with all parsed csv lines by cotation
     * and series root lines list
     *
     * @throws SEDALibException     if csv file can't be accessed or is badly formatted
     * @throws InterruptedException if import process is interrupted
     */
    private void readCSVFile() throws SEDALibException, InterruptedException {
        Line line;
        int lineCount = 0;
        linesMap = new HashMap<String, List<Line>>();

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(String[].class).withColumnSeparator(separator);
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(csvFileName), encoding);
             MappingIterator<String[]> it = mapper.readerFor(String[].class).with(schema).readValues(isr)) {
            while (it.hasNext()) {
                String[] row = it.next();
                lineCount++;
                // jump header line
                if (lineCount == 1)
                    continue;
                if (row.length < 5)
                    throw new SEDALibException("La ligne " + lineCount + " ne contient pas le nombre minimum de champs attendus (5)");
                line = new Line(row[0], row[1], row[2], row[3], row[4]);
                if (line.title.isEmpty())
                    throw new SEDALibException("Le titre de la ligne " + lineCount + " ne doit pas être vide");
                if (line.suffix.isEmpty())
                    throw new SEDALibException("Le suffixe de la ligne " + lineCount + " ne doit pas être vide");
                List<Line> value = linesMap.get(line.motherId);
                if (value == null)
                    value = new ArrayList<Line>();
                value.add(line);
                linesMap.put(line.motherId, value);
                if (sedaLibProgressLogger != null)
                    sedaLibProgressLogger.progressLogIfStep(SEDALibProgressLogger.OBJECTS_GROUP, lineCount, Integer.toString(lineCount) + " lignes interprétées");
            }
        } catch (IOException e) {
            throw new SEDALibException("Le fichier csv [" + csvFileName + "] n'est pas accessible");
        }
    }

    private ArchiveUnit addLineAndChildren(Line line) {
        ArchiveUnit au;
        Content content;

        au = new ArchiveUnit(dataObjectPackage);
        content = new Content();
        try {
            content.addNewMetadata("Title", line.title);
            if (!line.description.isEmpty())
                content.addNewMetadata("Description", line.description);
            if (line.motherId.isEmpty())
                content.addNewMetadata("DescriptionLevel", "Series");
            else
                content.addNewMetadata("DescriptionLevel", "Subseries");
            content.addNewMetadata("OriginatingAgencyArchiveUnitIdentifier", line.motherId + line.suffix);
        } catch (Exception ignored) {
        }
        au.setContent(content);
        List<Line> childLines = linesMap.get(line.motherId + line.suffix);
        if (childLines != null)
            for (Line childLine : linesMap.get(line.motherId + line.suffix)) {
                ArchiveUnit childAu = addLineAndChildren(childLine);
                au.addChildArchiveUnit(childAu);
            }
        linesMap.remove(line.motherId + line.suffix);

        return au;
    }

    /**
     * Do import the csv tree to ArchiveTransfer.
     *
     * @throws SEDALibException     if the csv can't be accessed or is badly formatted
     * @throws InterruptedException if export process is interrupted
     */
    public void doImport() throws SEDALibException, InterruptedException {
        String manifest;

        Date d = new Date();
        start = Instant.now();
        if (sedaLibProgressLogger != null)
            sedaLibProgressLogger.log(SEDALibProgressLogger.GLOBAL, "Début de l'import du fichier csv d'arbre de plan de classement [" + csvFileName + "] date="
                    + DateFormat.getDateTimeInstance().format(d));

        readCSVFile();
        dataObjectPackage = new DataObjectPackage();

        List<Line> rootLines = linesMap.get("");
        if (rootLines == null)
            throw new SEDALibException("Le fichier csv ne contient pas de ligne décrivant de série (champ série vide)");

        for (Line rootLine : rootLines) {
            ArchiveUnit rootAu = addLineAndChildren(rootLine);
            dataObjectPackage.addRootAu(rootAu);
        }

        if (linesMap.size() > 1) {
            String error = "Les lignes ayant les identifiants suivant n'appartiennent pas à une série  [";
            Iterator it = linesMap.keySet().iterator();
            while (it.hasNext()) {
                for (Line line : linesMap.get(it.next()))
                    error += line.uniqId + ", ";
            }
            error = error.substring(0, error.length() - 2) + "]";
            throw new SEDALibException(error);
        }

        end = Instant.now();
        if (sedaLibProgressLogger != null)
            sedaLibProgressLogger.log(SEDALibProgressLogger.GLOBAL, getSummary());
    }

    /**
     * Gets the data object pacakge.
     *
     * @return the data object package
     */
    public DataObjectPackage getDataObjectPackage() {
        return dataObjectPackage;
    }

    /**
     * Gets the summary of the import process.
     *
     * @return the summary
     */
    public String getSummary() {
        String result;

        result = "Import depuis un fichier CSV d'arbre de classement\n";
        result += "en [" + csvFileName + "]\n";
        result += dataObjectPackage.getDescription() + "\n";
        if (start != null)
            result += "chargé en "
                    + Duration.between(start, end).toString().substring(2) + "\n";
        return result;
    }

}
