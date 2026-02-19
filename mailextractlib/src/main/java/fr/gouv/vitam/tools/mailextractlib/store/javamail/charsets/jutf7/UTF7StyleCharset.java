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
package fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.jutf7;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Abstract base class for UTF-7 style encoding and decoding.</p>
 *
 * @author Jaap Beetstra
 */
abstract class UTF7StyleCharset extends Charset {

    private static final List CONTAINED = Arrays.asList(
        new String[] { "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16", "UTF-16LE", "UTF-16BE" }
    );
    final boolean strict;
    Base64Util base64;

    /**
     * <p>Besides the name and aliases, two additional parameters are required. First the
     * base 64 alphabet used; in modified UTF-7 a slightly different alphabet is used.
     * Additionally, it should be specified if encoders and decoders should be strict
     * about the interpretation of malformed encoded sequences. This is used since
     * modified UTF-7 specifically disallows some constructs which are allowed (or not
     * specifically disallowed) in UTF-7 (RFC 2152).</p>
     *
     * @param canonicalName The name as defined in java.nio.charset.Charset
     * @param aliases The aliases as defined in java.nio.charset.Charset
     * @param alphabet The base 64 alphabet used
     * @param strict True if strict handling of sequences is requested
     */
    protected UTF7StyleCharset(String canonicalName, String[] aliases, String alphabet, boolean strict) {
        super(canonicalName, aliases);
        this.base64 = new Base64Util(alphabet);
        this.strict = strict;
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#contains(java.nio.charset.Charset)
     */
    public boolean contains(final Charset cs) {
        return CONTAINED.contains(cs.name());
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#newDecoder()
     */
    public CharsetDecoder newDecoder() {
        return new UTF7StyleCharsetDecoder(this, base64, strict);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#newEncoder()
     */
    public CharsetEncoder newEncoder() {
        return new UTF7StyleCharsetEncoder(this, base64, strict);
    }

    /**
     * Tells if a character can be encoded using simple (US-ASCII) encoding or
     * requires base 64 encoding.
     *
     * @param ch The character
     * @return True if the character can be encoded directly, false otherwise
     */
    abstract boolean canEncodeDirectly(char ch);

    /**
     * Returns character used to switch to base 64 encoding.
     * @return The shift character
     */
    abstract byte shift();

    /**
     * Returns character used to switch from base 64 encoding to simple encoding.
     * @return The unshift character
     */
    abstract byte unshift();
}
