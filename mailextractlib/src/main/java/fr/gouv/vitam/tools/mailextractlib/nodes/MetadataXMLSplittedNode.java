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

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for XML node with only one long String value that has to be splitted
 * in many XML elements with the same tag of at most 32ko characters.
 * The value is splitted on a line separator or at least a space if possible not too
 * far from 32000.
 * <p>
 * This class is aimed to construct and write metadata XML
 * structure.
 */
public class MetadataXMLSplittedNode extends MetadataXMLNode {

    public static final int MAX_TEXT_LENGTH = 32765;

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.nodes.MetaDataXML#writeXML(int)
     */
    protected String writeXML(int depth) {
        String result = "";
        String tabs = depthTabs(depth);

        //first normalise value String
        String normalisedValue;
        normalisedValue = ((MetadataXMLString) value).getValue();
        normalisedValue = StringEscapeUtils.unescapeHtml4(normalisedValue);
        Matcher m = MetadataXMLString.HTML_PATTERN.matcher(normalisedValue);
        int iter = 0;
        if (m.find()) {
            StringBuffer sb = new StringBuffer();
            do {
                iter++;
                m.appendReplacement(sb, m.group().substring(0, 1) + " " + m.group().substring(1));
            } while (m.find());
            m.appendTail(sb);
            normalisedValue = sb.toString();
        }
        normalisedValue = StringEscapeUtils.escapeXml10(normalisedValue);

        // then write by 32765
        try {
            int i,imax;
            char c;
            while (normalisedValue.getBytes("UTF-8").length > MAX_TEXT_LENGTH) {
                int whiteSpaceSplitPlace;
                String subValue;
                imax = Math.min(MAX_TEXT_LENGTH,normalisedValue.length()-1);
                // try first to split on line, if not possible split on whitespace, and try to reduce till < 32766
                while (true) {
                    whiteSpaceSplitPlace = 0;
                    for (i = imax; i > imax-1000; i--) {
                        c = normalisedValue.charAt(i);
                        if ((c == '\r') || (c == '\n'))
                            break;
                        if ((whiteSpaceSplitPlace == 0) && Character.isWhitespace(c))
                            whiteSpaceSplitPlace = i;
                    }
                    if (i == imax-1000) {
                        if (whiteSpaceSplitPlace != 0)
                            i = whiteSpaceSplitPlace;
                        else i = imax;
                    }
                    subValue = normalisedValue.substring(0, i+1);
                    int encodedLength = subValue.getBytes("UTF-8").length;
                    if (encodedLength < 32766)
                        break;
                    imax = i - (encodedLength - 32766)-2;
                }
                result += tabs + "<" + tag;
                if (attributename != null)
                    result += " " + attributename + "=\"" + attributevalue + "\"";
                result += ">" + subValue + "</" + tag + ">\n";
                normalisedValue = normalisedValue.substring(i+1);
            }
            result += tabs + "<" + tag;
            if (attributename != null)
                result += " " + attributename + "=\"" + attributevalue + "\"";
            result += ">" + normalisedValue + "</" + tag + ">";
        } catch (UnsupportedEncodingException ignored) {
        }
        return result;
    }

    /**
     * Create an XML node for tag String tag and value String value.
     * <p>
     * Utility method for common case &lt;tag&gt;value&lt;\tag&gt;.
     *
     * @param tag   Tag
     * @param value MetaData object value
     */
    public MetadataXMLSplittedNode(String tag, String value) {
        super(tag, value);
    }

    /**
     * Create an XML node for tag String tag and value String value, with
     * attribute String attributename valued as String attributevalue.
     * <p>
     * Utility method for common case
     * &lt;tag attributename="attributevalue"&gt;value&lt;\tag&gt;.
     *
     * @param tag            Tag
     * @param attributename  Attribute name
     * @param attributevalue Attribute value
     * @param value          String object value
     */
    public MetadataXMLSplittedNode(String tag, String attributename, String attributevalue, String value) {
        super(tag, attributename, attributevalue, value);
    }
}
