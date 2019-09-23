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

import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;

/**
 * Class for extracting a Person metadata from an address.
 */
public class MetadataPerson {

    /**
     * The full name.
     */
    public String fullName;

    /**
     * The identifier.
     */
    public String identifier;

    /**
     * Construct a person from an address encoded. Example: TOTO John
     * Do&lt;toto@sample.fr&gt;" is extracted as:
     * <p>
     * FullName="TOTO John Do", Identifier="toto@sample.fr"
     * <p>
     *
     * @param s the address string
     */
    public MetadataPerson(String s) {
        int beg, end;
        String name = null;

        if ((s == null) || s.isEmpty()) {
            identifier = "[Vide]";
            fullName = "[Vide]";
            return;
        }

        if (((beg = s.lastIndexOf('<')) != -1) && ((end = s.lastIndexOf('>')) != -1) && (beg < end)) {
            identifier = s.substring(beg + 1, end).trim();
            if (identifier.isEmpty())
                identifier = "[Vide]";
            name = s.substring(0, beg).trim();
        } else
            identifier = s.trim();

        if ((name == null) || name.isEmpty()) {
            if ((end = identifier.indexOf('@')) != -1)
                name = identifier.substring(0, end);
            else
                name = "[Vide]";
        }

        if (name.startsWith("=?"))
            try {
                name = MimeUtility.decodeText(name);
            } catch (UnsupportedEncodingException e) {
                // Don't care
            }

        if ((name.charAt(0) == '"') && (name.charAt(name.length() - 1) == '"'))
            name = name.substring(1, name.length() - 1);

        fullName = name.replaceAll("\\.", " ");
    }
}
