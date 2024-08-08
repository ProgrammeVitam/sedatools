package fr.gouv.vitam.tools.sedalib.inout.importer.model;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Utility class for metadata formatter.
 */
public class MetadataTag {

    public String name;
    public String value;
    public String attr;
    public MetadataTag parent;
    public LinkedHashMap<Integer, List<MetadataTag>> subTags;

    /**
     * Instantiates a new Metadata tag.
     *
     * @param name   the name
     * @param parent the parent
     */
    public MetadataTag(String name, MetadataTag parent) throws SEDALibException {
        if ((name != null) && (!name.matches("[a-zA-Z0-9_-]+"))) {
            throw new SEDALibException("Caractère interdit dans le tag XML [" + name + "]");
        }
        this.name = name;
        this.value = null;
        this.attr = null;
        this.subTags = null;
        this.parent = parent;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return name;
        } else {
            return parent + "." + name;
        }
    }
}