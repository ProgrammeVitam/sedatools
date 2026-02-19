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

/**
 * <p>The character set specified in RFC 2152. Two variants are supported using the encodeOptional
 * constructor flag</p>
 *
 * @see <a href="http://tools.ietf.org/html/rfc2152">RFC 2152</a>
 * @author Jaap Beetstra
 */
public class UTF7Charset extends UTF7StyleCharset {

    private static final String BASE64_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789+/";
    private static final String SET_D = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'(),-./:?";
    private static final String SET_O = "!\"#$%&*;<=>@[]^_`{|}";
    private static final String RULE_3 = " \t\r\n";
    final String directlyEncoded;

    public UTF7Charset(String name, String[] aliases, boolean includeOptional) {
        super(name, aliases, BASE64_ALPHABET, false);
        if (includeOptional) this.directlyEncoded = SET_D + SET_O + RULE_3;
        else this.directlyEncoded = SET_D + RULE_3;
    }

    /* (non-Javadoc)
     * @see com.beetstra.jutf7.UTF7StyleCharset#canEncodeDirectly(char)
     */
    boolean canEncodeDirectly(char ch) {
        return directlyEncoded.indexOf(ch) >= 0;
    }

    /* (non-Javadoc)
     * @see com.beetstra.jutf7.UTF7StyleCharset#shift()
     */
    byte shift() {
        return '+';
    }

    /* (non-Javadoc)
     * @see com.beetstra.jutf7.UTF7StyleCharset#unshift()
     */
    byte unshift() {
        return '-';
    }
}
