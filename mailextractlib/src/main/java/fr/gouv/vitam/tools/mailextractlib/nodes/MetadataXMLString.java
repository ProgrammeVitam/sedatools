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

/**
 * Class for a XML string value in metadata
 * <p>
 * This very simple class is aimed to construct and write metadata XML
 * structure.
 */
public class MetadataXMLString extends MetadataXML {

    /**
     * String value.
     */
    String value;

    /**
     * Instantiates a new MetadataXMLString.
     *
     * @param value String value
     */
    MetadataXMLString(String value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.gouv.vitam.tools.mailextract.core.MetaData#isEmpty()
     */
    public boolean isEmpty() {
        return (value == null) || value.isEmpty();
    }

    /**
     * Write the value in XML format.
     * <p>
     * The String is UTF-8 encoded with linefeed, carriagereturn and tabulation escaped and
     * &lt;,&amp;,&gt;,' and " XML encoded, and stripped from illegal characters.
     *
     * @param depth
     *            Depth used for tabulation (no use for this tree Metadata
     *            Structure which is always a terminal tree node)
     * @return the string
     */
    public String writeXML(int depth) {
        return StringEscapeUtils.escapeXml10(value);
    }
}
