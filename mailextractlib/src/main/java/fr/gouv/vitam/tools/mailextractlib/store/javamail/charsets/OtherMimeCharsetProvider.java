/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to provide
 * tools for construction and manipulation of SIP (Submission
 * Information Package) conform to the SEDA (Standard d’Échange
 * de données pour l’Archivage) standard.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.jutf7.ModifiedUTF7Charset;
import fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.jutf7.UTF7Charset;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A CharsetProvider that registers redirecting charsets, new charsets and last of all try to filter the charset name.
 * <p>
 * Each registered charset is an instance of {@link Charset}. Some are instances of {@link RedirectCharset} which
 * delegates its encoding and decoding operations to an underlying standard charset.
 * </p>
 */
public class OtherMimeCharsetProvider extends CharsetProvider {

    /**
     * A list of charsets registered by this provider.
     * <p>
     * For example, the MIME codes for the existing Java charsets are registered with alternate canonical names.
     * </p>
     */
    private static final List<Charset> charsets = Arrays.asList(
        new RedirectCharset("MACINTOSH", new String[] {}, "x-MacRoman"),
        new RedirectCharset("UNKNOWN", new String[] { "DEFAULT", "iso-8859-iso-8859-1" }, "ISO-8859-1"),
        new UTF7Charset(
            "UTF-7",
            new String[] { "UNICODE-1-1-UTF-7", "CSUNICODE11UTF7", "X-RFC2152", "X-RFC-2152" },
            false
        ),
        new UTF7Charset("X-UTF-7-OPTIONAL", new String[] { "X-RFC2152-OPTIONAL", "X-RFC-2152-OPTIONAL" }, true),
        new ModifiedUTF7Charset(
            "X-MODIFIED-UTF-7",
            new String[] {
                "X-IMAP-MODIFIED-UTF-7",
                "X-IMAP4-MODIFIED-UTF7",
                "X-IMAP4-MODIFIED-UTF-7",
                "X-RFC3501",
                "X-RFC-3501",
            }
        )
    );

    /**
     * Returns an iterator over the charsets provided by this provider.
     *
     * @return an iterator over the registered charsets.
     */
    @Override
    public Iterator<Charset> charsets() {
        return charsets.iterator();
    }

    /**
     * Retrieves a charset for the given charset name.
     * <p>
     * This method searches the registered charsets for a match against the canonical name or any of its aliases,
     * ignoring case.
     * </p>
     *
     * @param charsetName the name or alias of the requested charset.
     * @return the matching Charset if found; {@code null} otherwise.
     */
    @Override
    public Charset charsetForName(String charsetName) {
        for (Charset cs : charsets) {
            if (cs.name().equalsIgnoreCase(charsetName)) {
                return cs;
            }
            for (String alias : cs.aliases()) {
                if (alias.equalsIgnoreCase(charsetName)) {
                    return cs;
                }
            }
        }
        // last chance filtering the Charset name
        String filteredCharsetName = charsetName.replaceAll("[=_]", "-").replaceAll("[^a-zA-Z0-9_-]", "");
        if (!filteredCharsetName.equals(charsetName)) {
            try {
                return Charset.forName(filteredCharsetName);
            } catch (Exception ignored) {} //NO SONAR
        }
        return null;
    }
}
