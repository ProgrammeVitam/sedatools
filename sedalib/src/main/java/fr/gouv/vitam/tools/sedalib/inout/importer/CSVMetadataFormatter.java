package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.inout.importer.model.MetadataTag;
import fr.gouv.vitam.tools.sedalib.inout.importer.model.ValueAttrMetadataTag;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The Class CSVMetadataFormatter.
 * <p>
 * It's a utility class for {@link CSVMetadataToDataObjectPackageImporter} to analyse header line, and then interpret other lines through the formatter.
 */
public class CSVMetadataFormatter {


    private static final String ID = "id";
    private static final String FILE = "file";
    private static final String PARENTID = "parentid";
    private static final String PARENTFILE = "parentfile";
    private static final String OBJECTFILES = "objectfiles";
    /**
     * The first columns header names.
     */
    private static final List<String> MANDATORY_TRAILING_HEADERS = Arrays.asList(
            ID, FILE, PARENTID, PARENTFILE, OBJECTFILES
    );
    private MetadataTag rootTag, contentTag, managementTag;
    private List<ValueAttrMetadataTag> tagHeaderColumnMapping;
    private int numberOfMandatoryHeaderFound;
    private int columnCount;

    private Path baseDir;

    private int guidColumn;
    private int fileColumn;
    private int objectfilesColumn;
    private int parentGUIDColumn;
    private boolean isOnlyFile = false;


    private void analyseFirstColumns(String[] headerRow) throws SEDALibException {
        List<String> firstsMandatoryHeadersFound = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String header = headerRow[i].toLowerCase();
            if (!MANDATORY_TRAILING_HEADERS.contains(header)) {
                break;
            }
            firstsMandatoryHeadersFound.add(header);
        }
        numberOfMandatoryHeaderFound = firstsMandatoryHeadersFound.size();
        if (numberOfMandatoryHeaderFound == 1 && firstsMandatoryHeadersFound.contains(FILE)) {
            isOnlyFile = true;
            guidColumn = 0;
            fileColumn = 0;
            objectfilesColumn = -1;
            parentGUIDColumn = -1;
        } else if (numberOfMandatoryHeaderFound == 2 && firstsMandatoryHeadersFound.containsAll(List.of(FILE, PARENTFILE))) {
            guidColumn = firstsMandatoryHeadersFound.indexOf(FILE);
            fileColumn = guidColumn;
            objectfilesColumn = -1;
            parentGUIDColumn = firstsMandatoryHeadersFound.indexOf(PARENTFILE);
        } else if (numberOfMandatoryHeaderFound == 3 && firstsMandatoryHeadersFound.containsAll(List.of(FILE, PARENTFILE, ID))) {
            guidColumn = firstsMandatoryHeadersFound.indexOf(ID);
            fileColumn = firstsMandatoryHeadersFound.indexOf(FILE);
            objectfilesColumn = -1;
            parentGUIDColumn = firstsMandatoryHeadersFound.indexOf(PARENTFILE);
        } else if (numberOfMandatoryHeaderFound == 3 && firstsMandatoryHeadersFound.containsAll(List.of(FILE, PARENTID, ID))) {
            guidColumn = firstsMandatoryHeadersFound.indexOf(ID);
            fileColumn = firstsMandatoryHeadersFound.indexOf(FILE);
            objectfilesColumn = -1;
            parentGUIDColumn = firstsMandatoryHeadersFound.indexOf(PARENTID);
        } else if (numberOfMandatoryHeaderFound == 3 && firstsMandatoryHeadersFound.containsAll(List.of(OBJECTFILES, PARENTID, ID))) {
            guidColumn = firstsMandatoryHeadersFound.indexOf(ID);
            fileColumn = -1;
            objectfilesColumn = firstsMandatoryHeadersFound.indexOf(OBJECTFILES);
            parentGUIDColumn = firstsMandatoryHeadersFound.indexOf(PARENTID);
        } else if (numberOfMandatoryHeaderFound == 4 && firstsMandatoryHeadersFound.containsAll(List.of(FILE, OBJECTFILES, PARENTID, ID))) {
            guidColumn = firstsMandatoryHeadersFound.indexOf(ID);
            objectfilesColumn = firstsMandatoryHeadersFound.indexOf(OBJECTFILES);
            fileColumn = firstsMandatoryHeadersFound.indexOf(FILE);
            parentGUIDColumn = firstsMandatoryHeadersFound.indexOf(PARENTID);
        } else {
            throw new SEDALibException("Le header [" + String.join("|", headerRow) + "] est mal formatté. Il doit contenir au début soit une colonne File, " +
                    "soit deux colonnes File et ParentFile, soit trois colonnes ID, File et ParentFile ou ID, File " +
                    ", soit quatre colonnes ID, ParentID, File, ObjectFiles " +
                    "et ParentID.");
        }
    }

    private MetadataTag getInSubTagsMap(MetadataTag tag, String name, int rank) throws SEDALibException {
        if (tag.subTags == null) {
            tag.subTags = new LinkedHashMap<>();
        } else if (tag.subTags.get(rank) != null) {
            for (MetadataTag subTag : tag.subTags.get(rank)) {
                if (subTag.name.equals(name)) {
                    return subTag;
                }
            }
        }

        MetadataTag subTag = new MetadataTag(name, tag);
        if (tag.name == null) {
            if (name.equals("Content")) {
                contentTag = subTag;
            } else if (name.equals("Management")) {
                managementTag = subTag;
            } else {
                throw new SEDALibException("Métadonnées [" + name + "] non conforme SEDA.");
            }
        }

        List<MetadataTag> subTags = tag.subTags.get(rank);
        if (subTags == null) {
            subTags = new ArrayList<>();
            subTags.add(subTag);
            tag.subTags.put(rank, subTags);
        } else {
            subTags.add(subTag);
        }

        return subTag;
    }

    private MetadataTag getTag(MetadataTag tag, List<String> splittedMetadataName) throws SEDALibException {
        if (splittedMetadataName.isEmpty()) {
            return tag;
        }
        String name = splittedMetadataName.get(0);
        if ((splittedMetadataName.size() == 1) && (name.equals("attr"))) {
            return tag;
        }
        int rank = 0;
        if (splittedMetadataName.size() > 1) {
            try {
                rank = Integer.parseInt(splittedMetadataName.get(1));
                splittedMetadataName.remove(0);
            } catch (Exception e) {
                //ignored
            }
        }
        MetadataTag subTag = getInSubTagsMap(tag, name, rank);
        splittedMetadataName.remove(0);
        return getTag(subTag, splittedMetadataName);
    }

    private void analyseTags(String[] headerRow) throws SEDALibException {
        MetadataTag currentTag = null;
        ValueAttrMetadataTag valueAttrMetadataTag;

        if (headerRow.length <= numberOfMandatoryHeaderFound) {
            throw new SEDALibException("Pas de colonne de métadonnées.");
        }
        if (headerRow[numberOfMandatoryHeaderFound].startsWith("Content.") || headerRow[numberOfMandatoryHeaderFound].startsWith("Management.")) {
            rootTag = new MetadataTag(null, null);
            contentTag = null;
        } else {
            rootTag = new MetadataTag("Content", null);
            contentTag = rootTag;
        }
        managementTag = null;
        tagHeaderColumnMapping = new ArrayList<>();
        for (int i = numberOfMandatoryHeaderFound; i < headerRow.length; i++) {
            String value = headerRow[i];
            if (value.equalsIgnoreCase("attr")) {
                if (currentTag == null) {
                    throw new SEDALibException("Le header attr en colonne n°" + i + " ne peut pas s'appliquer.");
                }
                valueAttrMetadataTag = new ValueAttrMetadataTag(false, currentTag);
            } else if (value.endsWith(".attr")) {
                currentTag = getTag(rootTag, new ArrayList<>(Arrays.asList(value.split("\\."))));
                valueAttrMetadataTag = new ValueAttrMetadataTag(false, currentTag);
            } else {
                currentTag = getTag(rootTag, new ArrayList<>(Arrays.asList(value.split("\\."))));

                valueAttrMetadataTag = new ValueAttrMetadataTag(true, currentTag);
            }
            tagHeaderColumnMapping.add(valueAttrMetadataTag);
        }
        if (contentTag == null) {
            throw new SEDALibException("Pas de colonne de métadonnées Content.");
        }
    }

    /**
     * Instantiates a new Metadata formatter.
     *
     * @param headerRow the header row
     * @param baseDir   the base dir
     * @throws SEDALibException the seda lib exception
     */
    public CSVMetadataFormatter(String[] headerRow, Path baseDir) throws SEDALibException {
        this.baseDir = baseDir;
        analyseFirstColumns(headerRow);
        analyseTags(headerRow);
        columnCount = headerRow.length;
    }

    private void resetValues() {
        tagHeaderColumnMapping.forEach(valueAttr -> {
            valueAttr.tag.value = null;
            valueAttr.tag.attr = null;
        });
    }

    private void defineColumnValue(int headerColumn, String cell) {
        ValueAttrMetadataTag vamt = tagHeaderColumnMapping.get(headerColumn - 1);
        if (vamt.isValue) {
            vamt.tag.value = cell;
        } else {
            vamt.tag.attr = cell;
        }
    }

    private String getOneSubTagXML(MetadataTag tag, String subTagName) {
        String result = "";
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList) {
                if (mt.name.equals(subTagName) && !mt.value.isEmpty()) {
                    result += "<" + subTagName + ">" + StringEscapeUtils.escapeXml10(mt.value) + "</" + subTagName + ">";
                    mt.value = null;
                }
            }
        }
        return result;
    }

    private boolean notEmptyValues(MetadataTag tag) {
        if (tag.subTags == null) {
            return StringUtils.isNotEmpty(tag.value);
        }
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag subTag : tagList) {
                if (notEmptyValues(subTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String generateRuleTypeTagXML(MetadataTag tag) throws SEDALibException {
        StringBuilder result = new StringBuilder();
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList) {
                if (mt.name.equals("Rule") && !mt.value.isEmpty()) {
                    result.append("<Rule>").append(StringEscapeUtils.escapeXml10(mt.value)).append("</Rule>");
                    mt.value = null;
                }
            }
            for (MetadataTag mt : tagList) {
                if (mt.name.equals("StartDate") && !mt.value.isEmpty()) {
                    result.append("<StartDate>").append(StringEscapeUtils.escapeXml10(mt.value)).append("</StartDate>");
                    mt.value = null;
                }
            }
        }
        result.append(getOneSubTagXML(tag, "PreventInheritance"));
        result.append(getOneSubTagXML(tag, "RefNonRuleId"));
        result.append(getOneSubTagXML(tag, "FinalAction"));

        if (notEmptyValues(tag)) {
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");
        }

        if (result.length() > 0) {
            result = new StringBuilder("<" + tag.name + ">" + result + "</" + tag.name + ">");
        }
        return result.toString();
    }

    private String generateHoldRuleTagXML(MetadataTag tag) throws SEDALibException {
        StringBuilder result = new StringBuilder();
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList) {
                if (!mt.value.isEmpty()) {
                    switch (mt.name) {
                        case "Rule":
                            result.append("<Rule>").append(StringEscapeUtils.escapeXml10(mt.value)).append("</Rule>");
                            break;
                        case "StartDate":
                            result.append("<StartDate>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</StartDate>");
                            break;
                        case "HoldEndDate":
                            result.append("<HoldEndDate>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</HoldEndDate>");
                            break;
                        case "HoldOwner":
                            result.append("<HoldOwner>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</HoldOwner>");
                            break;
                        case "HoldReassessingDate":
                            result.append("<HoldReassessingDate>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</HoldReassessingDate>");
                            break;
                        case "HoldReason":
                            result.append("<HoldReason>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</HoldReason>");
                            break;
                        case "PreventRearrangement":
                            result.append("<PreventRearrangement>").append(StringEscapeUtils.escapeXml10(mt.value))
                                    .append("</PreventRearrangement>");
                            break;
                        default:
                            continue;
                    }
                    mt.value = null;
                }
            }
        }
        result.append(getOneSubTagXML(tag, "PreventInheritance"));
        result.append(getOneSubTagXML(tag, "RefNonRuleId"));

        if (notEmptyValues(tag)) {
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");
        }

        if (result.length() > 0) {
            result = new StringBuilder("<" + tag.name + ">" + result + "</" + tag.name + ">");
        }
        return result.toString();
    }

    private String generateClassificationRuleTagXML(MetadataTag tag) throws SEDALibException {
        String result = "";
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList) {
                if (mt.name.equals("Rule") && !mt.value.isEmpty()) {
                    result += "<Rule>" + StringEscapeUtils.escapeXml10(mt.value) + "</Rule>";
                    mt.value = null;
                }
            }
            for (MetadataTag mt : tagList) {
                if (mt.name.equals("StartDate") && !mt.value.isEmpty()) {
                    result += "<StartDate>" + StringEscapeUtils.escapeXml10(mt.value) + "</StartDate>";
                    mt.value = null;
                }
            }
        }
        result += getOneSubTagXML(tag, "PreventInheritance");
        result += getOneSubTagXML(tag, "RefNonRuleId");
        result += getOneSubTagXML(tag, "FinalAction");
        result += getOneSubTagXML(tag, "ClassificationLevel");
        result += getOneSubTagXML(tag, "ClassificationOwner");
        result += getOneSubTagXML(tag, "ClassificationReassessingDate");
        result += getOneSubTagXML(tag, "NeedReassessingAuthorization");

        if (notEmptyValues(tag)) {
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");
        }

        if (!result.isEmpty()) {
            result = "<" + tag.name + ">" + result + "</" + tag.name + ">";
        }
        return result;
    }

    private String generateTagXML(MetadataTag tag) throws SEDALibException {
        String result;
        String value = "";

        // specific cases for RuleType and ClassificationRule
        switch (tag.name) {
            case "AccessRule":
            case "AppraisalRule":
            case "DisseminationRule":
            case "ReuseRule":
            case "StorageRule":
                return generateRuleTypeTagXML(tag);
            case "ClassificationRule":
                return generateClassificationRuleTagXML(tag);
            case "HoldRule":
                return generateHoldRuleTagXML(tag);
            default:
        }

        if (tag.subTags != null) {
            for (List<MetadataTag> tagList : tag.subTags.values()) {
                for (MetadataTag subTag : tagList) {
                    value += generateTagXML(subTag);
                }
            }
        }

        if (value.isEmpty()
                && ((tag.value == null) || tag.value.isEmpty())
                && ((tag.attr == null) || tag.attr.isEmpty())) {
            return "";
        }
        if (!value.isEmpty() && (tag.value != null) && !tag.value.isEmpty()) {
            throw new SEDALibException("Il ne peut y avoir une valeur et des sous-éléments dans un élément SEDA [" + tag + "].");
        }
        if (tag.name != null) {
            result = "<" + tag.name;
            if ((tag.attr != null) && !tag.attr.isEmpty()) {
                result += " " + tag.attr;
            }
            result += ">";
            if (!value.isEmpty()) {
                result += value;
            } else {
                result += StringEscapeUtils.escapeXml10(tag.value);
            }
            result += "</" + tag.name + ">";
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Do interpret and format the line content and extract the XML Content metadata
     *
     * @param row the array of cell strings from the csv line
     * @return the XML Content metadata
     * @throws SEDALibException the seda lib exception
     */
    public String doFormatAndExtractContentXML(String[] row) throws SEDALibException {
        if (row.length != columnCount) {
            throw new SEDALibException("Il n'y a pas le bon nombre d'éléments sur la ligne.");
        }
        resetValues();
        for (int i = numberOfMandatoryHeaderFound; i < row.length; i++) {
            defineColumnValue(i, row[i]);
        }
        return generateTagXML(contentTag);
    }

    /**
     * Extract the XML Management metadata
     *
     * @return the XML Management metadata or null
     * @throws SEDALibException the seda lib exception
     */
    public String extractManagementXML() throws SEDALibException {
        if (managementTag == null) {
            return "";
        }
        return generateTagXML(managementTag);
    }


    /**
     * Gets guid.
     *
     * @param row the array of cell strings from the csv line
     * @return the guid
     */
    public String getGUID(String[] row) {
        if (guidColumn == fileColumn) {
            return getFile(row);
        }
        return row[guidColumn];
    }

    /**
     * Gets parent guid.
     *
     * @param row the array of cell strings from the csv line
     * @return the parent guid
     */
    public String getParentGUID(String[] row) {
        if (isOnlyFile) {
            return Paths.get(getFile(row)).getParent().toString();
        }
        return row[parentGUIDColumn];
    }

    /**
     * Get file string.
     *
     * @param row the array of cell strings from the csv line
     * @return the string
     */
    public String getFile(String[] row) {
        return baseDir.resolve(row[fileColumn]).toAbsolutePath().normalize().toString();
    }

    /**
     * Gets object files list joined with the '|' character
     *
     * @param row the array of cell strings from the csv line
     * @return the object files list
     */
    public String getObjectFiles(String[] row) {
        if (objectfilesColumn == -1) {
            return "";
        } else {
            return row[objectfilesColumn];
        }
    }

    /**
     * Need ID regeneration.
     *
     * @return the need ID regeneration boolean
     */
    public boolean needIdRegeneration() {
        return isOnlyFile;
    }

    /**
     * Is extended format.
     *
     * @return is extended format boolean
     */
    public boolean isExtendedFormat() {
        return objectfilesColumn != -1;
    }
}
