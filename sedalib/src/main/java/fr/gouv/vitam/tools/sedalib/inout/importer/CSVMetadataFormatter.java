package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
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
    /**
     * Utility class for metadata formatter.
     */
    private class MetadataTag {

        String name;
        String value;
        String attr;
        MetadataTag parent;
        LinkedHashMap<Integer, List<MetadataTag>> subTags;

        /**
         * Instantiates a new Metadata tag.
         *
         * @param name   the name
         * @param parent the parent
         */
        protected MetadataTag(String name, MetadataTag parent) throws SEDALibException {
            if ((name != null) && (!name.matches("[a-zA-Z0-9_-]+")))
                throw new SEDALibException("Caractère interdit dans le tag XML [" + name + "]");
            this.name = name;
            this.value = null;
            this.attr = null;
            this.subTags = null;
            this.parent = parent;
        }

        @Override
        public String toString() {
            if (parent == null)
                return name;
            else
                return parent.toString() + "." + name;
        }
    }


    private class ValueAttrMetadataTag {
        boolean isValue;
        MetadataTag tag;

        /**
         * Instantiates a new Value attr metadata tag.
         *
         * @param isValue the is value
         * @param tag     the tag
         */
        public ValueAttrMetadataTag(boolean isValue, MetadataTag tag) {
            this.isValue = isValue;
            this.tag = tag;
        }
    }

    /**
     * The first columns header names.
     */
    private static List<String> headerNames = Arrays.asList("id", "file", "parentid", "parentfile","objectfiles");

    /**
     * The first columns types.
     */
    private static final int ID_COLUMN = 0;
    private static final int FILE_COLUMN = 1;
    private static final int PARENTID_COLUMN = 2;
    private static final int PARENTFILE_COLUMN = 3;
    private static final int OBJECTFIlES_COLUMN = 4;

    private MetadataTag rootTag, contentTag, managementTag;
    private LinkedHashMap<Integer, ValueAttrMetadataTag> tagHeaderColumnMapping;
    private int firstIndex;
    private int columnCount;

    private Path baseDir;

    private int guidColumn;
    private int fileColumn;
    private int objectfilesColumn;
    private int parentGUIDColumn;
    private boolean isOnlyFile;


    private void analyseFirstColumns(String[] headerRow) throws SEDALibException {
        List<Integer> firsts = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (!headerNames.contains(headerRow[i].toLowerCase()))
                break;
            firsts.add(headerNames.indexOf(headerRow[i].toLowerCase()));
        }
        firstIndex = firsts.size();
        if (((firstIndex == 1) && firsts.contains(FILE_COLUMN))) {
            isOnlyFile = true;
            guidColumn = 0;
            fileColumn = 0;
            objectfilesColumn=-1;
            parentGUIDColumn = -1;
        } else if ((firstIndex == 2) && firsts.contains(FILE_COLUMN) && firsts.contains(PARENTFILE_COLUMN)) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(FILE_COLUMN);
            fileColumn = guidColumn;
            objectfilesColumn=-1;
            parentGUIDColumn = firsts.indexOf(PARENTFILE_COLUMN);

        } else if ((firstIndex == 3) && (firsts.contains(FILE_COLUMN) && firsts.contains(PARENTFILE_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(ID_COLUMN);
            fileColumn = firsts.indexOf(FILE_COLUMN);
            objectfilesColumn=-1;
            parentGUIDColumn = firsts.indexOf(PARENTFILE_COLUMN);
        } else if ((firstIndex == 3) && (firsts.contains(FILE_COLUMN) && firsts.contains(PARENTID_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(ID_COLUMN);
            fileColumn = firsts.indexOf(FILE_COLUMN);
            objectfilesColumn=-1;
            parentGUIDColumn = firsts.indexOf(PARENTID_COLUMN);
        } else if ((firstIndex == 3) && (firsts.contains(OBJECTFIlES_COLUMN) && firsts.contains(PARENTID_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(ID_COLUMN);
            fileColumn = -1;
            objectfilesColumn=firsts.indexOf(OBJECTFIlES_COLUMN);
            parentGUIDColumn = firsts.indexOf(PARENTID_COLUMN);
        } else if ((firstIndex == 4) && (firsts.contains(FILE_COLUMN) && firsts.contains(OBJECTFIlES_COLUMN) && firsts.contains(PARENTID_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(ID_COLUMN);
            objectfilesColumn=firsts.indexOf(OBJECTFIlES_COLUMN);
            fileColumn = firsts.indexOf(FILE_COLUMN);
            parentGUIDColumn = firsts.indexOf(PARENTID_COLUMN);
        } else
            throw new SEDALibException("Le header [" + String.join("|", headerRow) + "] est mal formatté. Il doit contenir au début soit une colonne File, " +
                    "soit deux colonnes File et ParentFile, soit trois colonnes ID, File et ParentFile ou ID, File " +
                    ", soit quatre colonnes ID, ParentID, File, ObjectFiles " +
                    "et ParentID.");
    }

    private MetadataTag getInSubTagsMap(MetadataTag tag, String name, int rank) throws SEDALibException {
        if (tag.subTags != null) {
            if (tag.subTags.get(rank) != null)
                for (MetadataTag subTag : tag.subTags.get(rank))
                    if (subTag.name.equals(name))
                        return subTag;
        } else
            tag.subTags = new LinkedHashMap<>();
        MetadataTag subTag = new MetadataTag(name, tag);
        if (tag.name == null) {
            if (name.equals("Content"))
                contentTag = subTag;
            else if (name.equals("Management"))
                managementTag = subTag;
            else
                throw new SEDALibException("Métadonnées [" + name + "] non conforme SEDA.");
        }

        List<MetadataTag> subTags = tag.subTags.get(rank);
        if (subTags == null) {
            subTags = new ArrayList<>();
            subTags.add(subTag);
            tag.subTags.put(rank, subTags);
        } else
            subTags.add(subTag);

        return subTag;
    }

    private MetadataTag getTag(MetadataTag tag, List<String> splittedMetadataName) throws SEDALibException {
        if (splittedMetadataName.isEmpty())
            return tag;
        String name = splittedMetadataName.get(0);
        if ((splittedMetadataName.size() == 1) && (name.equals("attr")))
            return tag;
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
        ValueAttrMetadataTag vamt;

        if (headerRow.length <= firstIndex)
            throw new SEDALibException("Pas de colonne de métadonnées.");
        if (headerRow[firstIndex].startsWith("Content.") || headerRow[firstIndex].startsWith("Management.")) {
            rootTag = new MetadataTag(null, null);
            contentTag = null;
        } else {
            rootTag = new MetadataTag("Content", null);
            contentTag = rootTag;
        }
        managementTag = null;
        tagHeaderColumnMapping = new LinkedHashMap<>();
        for (int i = firstIndex; i < headerRow.length; i++) {
            if (headerRow[i].equalsIgnoreCase("attr")) {
                if (currentTag == null)
                    throw new SEDALibException("Le header attr en colonne n°" + i + " ne peut pas s'appliquer.");
                vamt = new ValueAttrMetadataTag(false, currentTag);
            } else if (headerRow[i].endsWith(".attr")) {
                currentTag = getTag(rootTag, new ArrayList<>(Arrays.asList(headerRow[i].split("\\."))));
                vamt = new ValueAttrMetadataTag(false, currentTag);
            } else {
                currentTag = getTag(rootTag, new ArrayList<>(Arrays.asList(headerRow[i].split("\\."))));
                vamt = new ValueAttrMetadataTag(true, currentTag);
            }
            tagHeaderColumnMapping.put(i, vamt);
        }
        if (contentTag == null)
            throw new SEDALibException("Pas de colonne de métadonnées Content.");
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
        tagHeaderColumnMapping.values().stream().forEach(valueAttr -> {
            valueAttr.tag.value = null;
            valueAttr.tag.attr = null;
        });
    }

    private void defineColumnValue(int headerColumn, String cell) {
        ValueAttrMetadataTag vamt = tagHeaderColumnMapping.get(headerColumn);
        if (vamt.isValue)
            vamt.tag.value = cell;
        else
            vamt.tag.attr = cell;
    }

    private String getOneSubTagXML(MetadataTag tag, String subTagName) {
        String result = "";
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList)
                if (mt.name.equals(subTagName) && !mt.value.isEmpty()) {
                    result += "<" + subTagName + ">" + StringEscapeUtils.escapeXml10(mt.value) + "</" + subTagName + ">";
                    mt.value = null;
                }
        }
        return result;
    }

    private boolean notEmptyValues(MetadataTag tag) {
        boolean result = false;
        if (tag.subTags == null)
            return !((tag.value == null)|| tag.value.isEmpty());
        for (List<MetadataTag> tagList : tag.subTags.values())
            for (MetadataTag subTag : tagList)
                result = result || notEmptyValues(subTag);
        return result;
    }

    private String generateRuleTypeTagXML(MetadataTag tag) throws SEDALibException {
        String result ="";
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList)
                if (mt.name.equals("Rule") && !mt.value.isEmpty()) {
                    result += "<Rule>" + StringEscapeUtils.escapeXml10(mt.value) + "</Rule>";
                    mt.value = null;
                }
            for (MetadataTag mt : tagList)
                if (mt.name.equals("StartDate") && !mt.value.isEmpty()) {
                    result += "<StartDate>" + StringEscapeUtils.escapeXml10(mt.value) + "</StartDate>";
                    mt.value = null;
                }
        }
        result += getOneSubTagXML(tag, "PreventInheritance");
        result += getOneSubTagXML(tag, "RefNonRuleId");
        result += getOneSubTagXML(tag, "FinalAction");

        if (notEmptyValues(tag))
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");

        if (!result.isEmpty())
            result = "<" + tag.name + ">"+result+"</" + tag.name + ">";
        return result;
    }

    private String generateHoldRuleTagXML(MetadataTag tag) throws SEDALibException {
        StringBuilder result = new StringBuilder();
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList) {
                if(!mt.value.isEmpty()) {
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
                        case  "HoldReason":
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

        if (notEmptyValues(tag))
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");

        if (result.length() > 0)
            result = new StringBuilder("<" + tag.name + ">" + result + "</" + tag.name + ">");
        return result.toString();
    }

    private String generateClassificationRuleTagXML(MetadataTag tag) throws SEDALibException {
        String result ="";
        for (List<MetadataTag> tagList : tag.subTags.values()) {
            for (MetadataTag mt : tagList)
                if (mt.name.equals("Rule") && !mt.value.isEmpty()) {
                    result += "<Rule>" + StringEscapeUtils.escapeXml10(mt.value) + "</Rule>";
                    mt.value = null;
                }
            for (MetadataTag mt : tagList)
                if (mt.name.equals("StartDate") && !mt.value.isEmpty()) {
                    result += "<StartDate>" + StringEscapeUtils.escapeXml10(mt.value) + "</StartDate>";
                    mt.value = null;
                }
        }
        result += getOneSubTagXML(tag, "PreventInheritance");
        result += getOneSubTagXML(tag, "RefNonRuleId");
        result += getOneSubTagXML(tag, "FinalAction");
        result += getOneSubTagXML(tag, "ClassificationLevel");
        result += getOneSubTagXML(tag, "ClassificationOwner");
        result += getOneSubTagXML(tag, "ClassificationReassessingDate");
        result += getOneSubTagXML(tag, "NeedReassessingAuthorization");

        if (notEmptyValues(tag))
            throw new SEDALibException("La règle [" + tag.name + "] contient des champs non conformes SEDA.");

        if (!result.isEmpty())
            result = "<" + tag.name + ">"+result+"</" + tag.name + ">";
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
            for (List<MetadataTag> tagList : tag.subTags.values())
                for (MetadataTag subTag : tagList)
                    value += generateTagXML(subTag);
        }

        if (value.isEmpty()
                && ((tag.value == null) || tag.value.isEmpty())
                && ((tag.attr == null) || tag.attr.isEmpty()))
            return "";
        if (!value.isEmpty() && (tag.value != null) && !tag.value.isEmpty())
            throw new SEDALibException("Il ne peut y avoir une valeur et des sous-éléments dans un élément SEDA [" + tag + "].");
        if (tag.name != null) {
            result = "<" + tag.name;
            if ((tag.attr != null) && !tag.attr.isEmpty())
                result += " " + tag.attr;
            result += ">";
            if (!value.isEmpty())
                result += value;
            else
                result += StringEscapeUtils.escapeXml10(tag.value);
            result += "</" + tag.name + ">";
        } else
            result = value;
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
        if (row.length != columnCount)
            throw new SEDALibException("Il n'y a pas le bon nombre d'éléments sur la ligne.");
        resetValues();
        for (int i = firstIndex; i < row.length; i++)
            defineColumnValue(i, row[i]);
        return generateTagXML(contentTag);
    }

    /**
     * Extract the XML Management metadata
     *
     * @return the XML Management metadata or null
     * @throws SEDALibException the seda lib exception
     */
    public String extractManagementXML() throws SEDALibException {
        if (managementTag == null)
            return "";
        return generateTagXML(managementTag);
    }


    /**
     * Gets guid.
     *
     * @param row the array of cell strings from the csv line
     * @return the guid
     */
    public String getGUID(String[] row) {
        if (guidColumn == fileColumn)
            return getFile(row);
        return row[guidColumn];
    }

    /**
     * Gets parent guid.
     *
     * @param row the array of cell strings from the csv line
     * @return the parent guid
     */
    public String getParentGUID(String[] row) {
        if (isOnlyFile)
            return Paths.get(getFile(row)).getParent().toString();
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
        if (objectfilesColumn==-1)
            return "";
        else
            return row[objectfilesColumn];
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
        return objectfilesColumn!=-1;
    }
}
