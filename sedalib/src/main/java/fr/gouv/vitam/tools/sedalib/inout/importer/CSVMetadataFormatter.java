package fr.gouv.vitam.tools.sedalib.inout.importer;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
        Map<Integer, List<MetadataTag>> subTags;

        /**
         * Instantiates a new Metadata tag.
         *
         * @param name   the name
         * @param parent the parent
         */
        protected MetadataTag(String name, MetadataTag parent) throws SEDALibException {
            if (!name.matches("[a-zA-Z0-9_-]+"))
                throw new SEDALibException("Caractère interdit dans le tag XML ["+name+"]");
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
    static private List<String> headerNames = Arrays.asList("id", "file", "parentid", "fileid");

    /**
     * The first columns types.
     */
    static final private int ID_COLUMN = 0;
    static final private int FILE_COLUMN = 1;
    static final private int PARENTID_COLUMN = 2;
    static final private int PARENTFILE_COLUMN = 3;

    private MetadataTag contentTag;
    private Map<Integer, ValueAttrMetadataTag> tagHeaderColumnMapping;
    private int firstIndex;

    private Path baseDir;

    private int guidColumn;
    private int fileColumn;
    private int parentGUIDColumn;
    private boolean isOnlyFile;


    private void analyseFirstColumns(String[] headerRow) throws SEDALibException {
        List<Integer> firsts = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            if (!headerNames.contains(headerRow[i].toLowerCase()))
                break;
            firsts.add(headerNames.indexOf(headerRow[i].toLowerCase()));
        }
        firstIndex=firsts.size();
        if (((firstIndex == 1) && firsts.contains(FILE_COLUMN))) {
            isOnlyFile = true;
            guidColumn = 0;
            fileColumn = 0;
            parentGUIDColumn = -1;
        } else if ((firstIndex == 2) && firsts.contains(FILE_COLUMN) && firsts.contains(PARENTFILE_COLUMN)) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(FILE_COLUMN);
            fileColumn = guidColumn;
            parentGUIDColumn = firsts.indexOf(PARENTFILE_COLUMN);

        } else if ((firstIndex == 3) && (firsts.contains(FILE_COLUMN) && firsts.contains(PARENTFILE_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(FILE_COLUMN);
            fileColumn = guidColumn;
            parentGUIDColumn = firsts.indexOf(PARENTFILE_COLUMN);
        } else if ((firstIndex == 3) &&
                (firsts.contains(FILE_COLUMN) && firsts.contains(PARENTID_COLUMN) && firsts.contains(ID_COLUMN))) {
            isOnlyFile = false;
            guidColumn = firsts.indexOf(ID_COLUMN);
            fileColumn = firsts.indexOf(FILE_COLUMN);
            parentGUIDColumn = firsts.indexOf(PARENTID_COLUMN);
        } else
            throw new SEDALibException("Le header est mal formatté. Il doit contenir au début soit une colonne File, " +
                    "soit deux colonnes File et ParentFile, soit trois colonnes ID, File et ParentFile ou ID, File " +
                    "et ParentID.");
    }

    private MetadataTag getInSubTagsMap(MetadataTag tag, String name, int rank) throws SEDALibException {
        if (tag.subTags != null) {
            if (tag.subTags.get(rank) != null)
                for (MetadataTag subTag : tag.subTags.get(rank))
                    if (subTag.name.equals(name))
                        return subTag;
        } else
            tag.subTags = new HashMap<Integer, List<MetadataTag>>();
        MetadataTag subTag = new MetadataTag(name, tag);
        List<MetadataTag> subTags = tag.subTags.get(rank);
        if (subTags == null) {
            subTags = new ArrayList<MetadataTag>();
            subTags.add(subTag);
            tag.subTags.put(rank, subTags);
        } else
            subTags.add(subTag);
        return subTag;
    }

    private MetadataTag getTag(MetadataTag tag, List<String> splittedMetadataName) throws SEDALibException {
        if (splittedMetadataName.size() == 0)
            return tag;
        String name = splittedMetadataName.get(0);
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
        contentTag = new MetadataTag("Content", null);
        tagHeaderColumnMapping = new HashMap<Integer, ValueAttrMetadataTag>();
        for (int i = firstIndex; i < headerRow.length; i++) {
            if (headerRow[i].toLowerCase().equals("attr")) {
                if (currentTag == null)
                    throw new SEDALibException("Le header attr en colonne n°" + i + " ne peut pas s'appliquer.");
                tagHeaderColumnMapping.put(i, new ValueAttrMetadataTag(false, currentTag));
            } else {
                currentTag = getTag(contentTag, new ArrayList(Arrays.asList(headerRow[i].split("\\."))));
                tagHeaderColumnMapping.put(i, new ValueAttrMetadataTag(true, currentTag));
            }
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
        this.baseDir=baseDir;
        analyseFirstColumns(headerRow);
        analyseTags(headerRow);
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

    private String generateTagXML(MetadataTag tag) throws SEDALibException {
        String result, value = "";

        if (tag.subTags != null) {
            for (List<MetadataTag> tagList : tag.subTags.values())
                for (MetadataTag subTag : tagList)
                    value += generateTagXML(subTag);
        }

        if (value.isEmpty()
                && ((tag.value == null) || tag.value.isEmpty())
                && ((tag.attr == null) || tag.value.isEmpty()))
            return "";
        if (!value.isEmpty() && (tag.value != null) && !tag.value.isEmpty())
            throw new SEDALibException("Il ne peut y avoir une valeur et des sous-éléments dans un élément XML [" + tag + "].");
        result = "<" + tag.name;
        if ((tag.attr != null) && !tag.attr.isEmpty())
            result += " " + tag.attr;
        result += ">";
        if (!value.isEmpty())
            result += value;
        else
            result += StringEscapeUtils.escapeXml10(tag.value);
        result += "</" + tag.name + ">";
        return result;
    }

    /**
     * Do interpret and format the line content has an XML Content metadata
     *
     * @param row the array of cell strings from the csv line
     * @return the XML Content metadata
     * @throws SEDALibException the seda lib exception
     */
    public String doformatXML(String[] row) throws SEDALibException {
        resetValues();
        for (int i = firstIndex; i < row.length; i++)
            defineColumnValue(i, row[i]);
        return generateTagXML(contentTag);
    }


    /**
     * Gets guid.
     *
     * @param row the array of cell strings from the csv line
     * @return the guid
     */
    public String getGUID(String[] row) {
        if (guidColumn==fileColumn)
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
    public String getFile(String[] row){
        return baseDir.resolve(row[fileColumn]).toAbsolutePath().normalize().toString();
    }
}
