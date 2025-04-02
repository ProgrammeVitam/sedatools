/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

package fr.gouv.vitam.tools.mailextractlib.nodes;

/**
 * Class for one XML node in metadata with one attribute possibility
 * <p>
 * This very simple class is aimed to construct and write metadata XML
 * structure.
 */
public class MetadataXMLNode extends MetadataXML {

    String tag;
    String attributename;
    String attributevalue;
    MetadataXML value;

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
     */
    public boolean isEmpty() {
        return value == null;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#writeJSON(int)
     */
    protected String writeXML(int depth) {
        StringBuilder sb = new StringBuilder();
        String tabs = depthTabs(depth);

        sb.append(tabs)
                .append('<').append(tag);

        if (attributename != null) {
            sb.append(' ')
                    .append(attributename)
                    .append("=\"")
                    .append(attributevalue)
                    .append('"');
        }

        sb.append('>');

        if (value instanceof MetadataXMLString) {
            sb.append(value.writeXML(depth + 1));
        } else {
            sb.append('\n')
                    .append(value.writeXML(depth + 1))
                    .append('\n')
                    .append(tabs);
        }

        sb.append("</")
                .append(tag)
                .append('>');

        return sb.toString();
    }

    /**
     * Create an empty XML node for tag String tag.
     *
     * @param tag
     *            Tag
     */
    public MetadataXMLNode(String tag) {
        this.tag = tag;
        this.attributename = null;
        this.attributevalue = null;
        this.value = null;
    }

    /**
     * Create an XML node for tag String tag and value MetadataXML value.
     *
     * @param tag
     *            Tag
     * @param value
     *            MetadataXML object value
     */
    public MetadataXMLNode(String tag, MetadataXML value) {
        this.tag = tag;
        this.attributename = null;
        this.attributevalue = null;
        this.value = value;
    }

    /**
     * Create an XML node for tag String tag and value MetadataXML value, with
     * attribute String attributename valued as String attributevalue.
     *
     * @param tag
     *            Tag
     * @param attributename
     *            Attribute name
     * @param attributevalue
     *            Attribute value
     * @param value
     *            MetadataXML object value
     */
    public MetadataXMLNode(String tag, String attributename, String attributevalue, MetadataXML value) {
        this.tag = tag;
        this.attributename = attributename;
        this.attributevalue = attributevalue;
        this.value = value;
    }

    /**
     * Create an XML node for tag String tag and value String value.
     * <p>
     * Utility method for common case &lt;tag&gt;value&lt;\tag&gt;.
     *
     * @param tag
     *            Tag
     * @param value
     *            MetaData object value
     */
    public MetadataXMLNode(String tag, String value) {
        this.tag = tag;
        this.attributename = null;
        this.attributevalue = null;
        this.value = new MetadataXMLString(value);
    }

    /**
     * Create an XML node for tag String tag and value String value, with
     * attribute String attributename valued as String attributevalue.
     * <p>
     * Utility method for common case
     * &lt;tag attributename="attributevalue"&gt;value&lt;\tag&gt;.
     *
     * @param tag
     *            Tag
     * @param attributename
     *            Attribute name
     * @param attributevalue
     *            Attribute value
     * @param value
     *            String object value
     */
    public MetadataXMLNode(String tag, String attributename, String attributevalue, String value) {
        this.tag = tag;
        this.attributename = attributename;
        this.attributevalue = attributevalue;
        this.value = new MetadataXMLString(value);
    }

    /**
     * Write the metadata in XML format.
     * <p>
     * The root XML structure is always of node nature and this method is the
     * only public one to write XML tree, beginning at tabs depth 0.
     *
     * @return the string
     */
    public String writeXML() {
        return writeXML(0);
    }

}
