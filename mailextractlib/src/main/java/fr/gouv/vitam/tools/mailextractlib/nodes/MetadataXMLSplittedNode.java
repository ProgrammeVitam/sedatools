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


import fr.gouv.vitam.tools.mailextractlib.utils.HtmlAndXmlEscape;

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

    public static final int MAX_TEXT_LENGTH = 32000;

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
     */
    public boolean isEmpty() {
        return (value == null) || value.isEmpty();
    }

    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile("[\\p{C}&&[^\\r\\n\\t]]");
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&([a-zA-Z][a-zA-Z0-9]+;)");

    /**
     * Normalise long string to prevent XML misinterpretation.
     * <p>
     * First unescape all HMTL4 entities, then still existing HTML structures are broken,
     * to be accepted by Vitam sanitizer, if any is detected a space is inserted after the
     * first character, either "&gt;" or "&amp;".
     * Then the String is UTF-8 encoded with linefeed, carriagereturn and tabulation escaped and
     * &lt;,&amp;,&gt;,' and " XML encoded, and stripped from illegal characters.
     * It's a little bit less accurate than the MetadataXMLString one but more efficient and resilient
     * to strange content
     *
     * @param value string to be normalised
     * @return the string
     */
    private static String normaliseXMLLongString(String value) {
        // remove all forbidden and invisible characters
        String normalized = FORBIDDEN_PATTERN.matcher(value).replaceAll("");
        // unescape all HMTL characters
        normalized= HtmlAndXmlEscape.unescapeHtmlAndXMLEntities(normalized);
        // break HTML tags in metadata if any
        normalized = normalized.replace("<", "< ");
        // break left HTML escape after unescape
        normalized = HTML_ENTITY_PATTERN.matcher(normalized).replaceAll("& $1");

        // suppress and escape all non XML compliance
        return HtmlAndXmlEscape.escapeXml(normalized);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.nodes.MetaDataXML#writeXML(int)
     */
    protected String writeXML(int depth) {
        StringBuilder result = new StringBuilder();
        String tabs = depthTabs(depth);

        // 1) Retrieve and "normalize" the value
        String normalisedValue;
        normalisedValue = normaliseXMLLongString(((MetadataXMLString) value).getValue());

        // 2) Write in chunks of up to MAX_TEXT_LENGTH
        int chunkBeg = 0;
        while (chunkBeg < normalisedValue.length()) {
            // findCutPosition should return a cutting point > chunkBeg
            int chunkEnd = findCutPosition(normalisedValue, chunkBeg, MAX_TEXT_LENGTH);

            // If findCutPosition fails to advance, we must avoid infinite loop.
            // As a fallback, we take the rest of the string in one shot.
            if (chunkEnd <= chunkBeg) {
                chunkEnd = normalisedValue.length();
            }

            // Extract substring
            String subValue = normalisedValue.substring(chunkBeg, chunkEnd);

            // Build the XML output for this chunk
            result.append(tabs)
                    .append('<').append(tag);
            if (attributename != null) {
                result.append(' ')
                        .append(attributename)
                        .append("=\"")
                        .append(attributevalue)
                        .append("\"");
            }
            result.append('>')
                    .append(subValue)
                    .append("</")
                    .append(tag)
                    .append(">\n");

            // Advance to the next segment
            chunkBeg = chunkEnd;
        }

        return result.toString();

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

    /**
     * Finds a position to cut the string so that the resulting UTF-8 fragment
     * from 'beg' does not exceed 'maxBytesLength' bytes.
     * Priority for newline, then space, then immediate cut.
     *
     * @param s              the source string
     * @param beg            the beginning index in the string from which to start counting
     * @param maxBytesLength the maximum number of UTF-8 bytes allowed
     * @return the index at which to cut
     */
    private static int findCutPosition(String s, int beg, int maxBytesLength) {
        final int length = s.length();
        int i = beg;
        int bytesCount = 0;

        // Store -1 to indicate "not encountered yet"
        int lastSpace = -1;
        int lastReturn = -1;

        if ((length - beg) * 3 < maxBytesLength)
            return s.length();

        while (i < length) {
            char c = s.charAt(i);

            // Inline UTF-8 length calculation
            int utf8len;
            if (c <= 0x7F) {
                utf8len = 1;
            } else if (c <= 0x7FF) {
                utf8len = 2;
            } else if (Character.isHighSurrogate(c)) {
                // We assume there's a valid Low Surrogate next if needed
                // Otherwise it's a more complex scenario
                utf8len = 4;
            } else {
                utf8len = 3;
            }

            bytesCount += utf8len;

            // Check if adding this character would exceed maxBytesLength
            if (bytesCount > maxBytesLength) {
                break;
            }

            // Accept this character

            // Remember positions of space or newline
            if (c == '\r' || c == '\n') {
                lastReturn = i + 1;
            } else if (Character.isSpaceChar(c)) {
                lastSpace = i + 1;
            }
            i++;
        }

        // If we haven't exceeded the limit, return the position we reached
        if (bytesCount < maxBytesLength) {
            return i;
        }

        // If a newline was encountered, prioritize cutting there
        if (lastReturn != -1) {
            return lastReturn;
        }

        // Otherwise, if a space was encountered, cut there
        if (lastSpace != -1) {
            return lastSpace;
        }

        // Fallback: cut exactly where we stopped
        return i;
    }
}
