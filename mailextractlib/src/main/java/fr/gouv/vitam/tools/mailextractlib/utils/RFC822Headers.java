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

package fr.gouv.vitam.tools.mailextractlib.utils;

import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * RFC822Headers class for extending JavaMail InternetHeaders with common
 * extraction methods.
 */
public class RFC822Headers extends InternetHeaders {

    /**
     * The message.
     */
    StoreMessage message;

    /**
     * Gets the bais.
     *
     * @param headersString the headers string
     * @return the bais
     */
    static ByteArrayInputStream getBAIS(String headersString) {
        if (headersString == null)
            headersString = "";
        headersString += "\n\n";
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(headersString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ignored) {
        }
        return bais;
    }

    /**
     * Instantiates a new RFC 822 headers.
     *
     * @param headersString the headers string
     * @param message       the message
     * @throws MessagingException the messaging exception
     */
    public RFC822Headers(String headersString, StoreMessage message) throws MessagingException {
        super(getBAIS(headersString), true);
        // no use to close on ByteArrayInputStream
        this.message = message;
    }

    /**
     * Gets the header value.
     *
     * @param line the line
     * @return the header value
     */
// utility function to get the value part of an header string
    public static String getHeaderValue(String line) {
        int i = line.indexOf(':');
        if (i < 0)
            return line;
        // skip whitespace after ':'
        int j;
        for (j = i + 1; j < line.length(); j++) {
            char c = line.charAt(j);
            if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n'))
                break;
        }
        return line.substring(j);
    }

    /**
     * Gets the references.
     *
     * @return the references
     */
    public List<String> getReferences() {
        List<String> result = null;
        String refHeader = getHeader("References", " ");
        if (refHeader != null) {
            result = new ArrayList<String>();
            String[] refList = getHeaderValue(refHeader).split(" ");
            for (String tmp : refList)
                try {
                    result.add(MimeUtility.decodeText(tmp));
                } catch (UnsupportedEncodingException ignored) {
                }
        }
        return result;
    }

    static private String getElementalStringAddress(InternetAddress address) {
        String result;
        String s;

        if (address != null) {
            s = address.getPersonal();
            if (s != null)
                result = s + " ";
            else
                result = "";
            s = address.getAddress();
            if (s != null)
                result += "<" + s + ">";
        } else
            result = "";
        return result;
    }

    static private String getStringAddress(InternetAddress address) {
        String result;

        if (address != null) {
            try {
                result = getElementalStringAddress(address);
                // special case of group address (RFC 2822)
                if (address.isGroup()) {
                    result += ":";
                    InternetAddress[] group = address.getGroup(false);
                    for (int k = 0; k < group.length; k++) {
                        if (k > 0)
                            result += ",";
                        result += getElementalStringAddress(group[k]);
                    }
                }
            } catch (AddressException e) {
                // not supposed to be
                result = "";
            }
        } else
            result = "";
        return result;
    }

    /**
     * Gets the address header.
     *
     * @param name the name
     * @return the address header
     * @throws InterruptedException the interrupted exception
     */
    public List<String> getAddressHeader(String name) throws InterruptedException {
        List<String> result = null;
        String addressHeaderString;

        addressHeaderString = getHeader(name, ", ");

        if (addressHeaderString != null) {
            result = new ArrayList<String>();
            InternetAddress[] iAddressArray;
            try {
                iAddressArray = InternetAddress.parseHeader(addressHeaderString, false);
            } catch (AddressException e) {
                try {
                    // try at least to Mime decode
                    addressHeaderString = MimeUtility.decodeText(addressHeaderString);
                } catch (UnsupportedEncodingException ignored) {
                }
                message.logMessageWarning("mailextractlib.rfc822: wrongly formatted address " + addressHeaderString
                        + ", keep raw address list in metadata in header " + name, e);
                result.add(addressHeaderString);
                return result;
            }
            for (InternetAddress ia : iAddressArray) {
                result.add(getStringAddress(ia));
            }
        }
        return result;
    }
}
