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
package fr.gouv.vitam.tools.mailextractlib.utils;

import fr.gouv.vitam.tools.mailextractlib.core.StoreMessage;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeUtility;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            if ((s != null) && !s.isEmpty())
                result = s + " ";
            else
                result = "";
            s = address.getAddress();
            if ((s != null) && !s.isEmpty())
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
        String addressHeaderString;

        addressHeaderString = getHeader(name, ", ");
        return treatAddressHeaderString(name, message, addressHeaderString);
    }


    /**
     * Processes a raw address header string and returns a list of email addresses.
     * <p>
     * If the header is well-formed, it extracts and cleans the individual addresses.
     * If the header is malformed, it attempts to decode it and logs a warning,
     * then returns the original header as a single entry.
     * </p>
     *
     * @param name                the header name (e.g., "To", "Cc")
     * @param message             the message object used for logging warnings
     * @param addressHeaderString the raw address header string to process
     * @return a list of processed email address strings
     */
    public static List<String> treatAddressHeaderString(String name, StoreMessage message, String addressHeaderString) throws InterruptedException {
        List<String> result = new ArrayList<String>();

        if (addressHeaderString != null) {
            InternetAddress[] iAddressArray;
            try {
                iAddressArray = InternetAddress.parseHeader(addressHeaderString, false);
            } catch (AddressException e) {
                // try at least to Mime decode
                addressHeaderString = decodeRfc2047Flexible(addressHeaderString);
                message.logMessageWarning("mailextractlib.rfc822: wrongly formatted address " + addressHeaderString
                        + ", keep raw address list in metadata in header " + name, e);
                result.add(addressHeaderString);
                return result;
            }
            for (InternetAddress ia : iAddressArray) {
                String address = getStringAddress(ia);
                if (address.contains("=?"))
                    address = decodeRfc2047Flexible(address);
                result.add(address);
            }
        }
        return result;
    }

    // RFC 2047 block patterns
    private static final Pattern ENCODED_WORD_PATTERN_BEGIN = Pattern.compile(
            "(=\\?[^?]+\\?[BbQq]\\?[^?]+)");
    private static final Pattern ENCODED_WORD_PATTERN = Pattern.compile(
            "(=\\?[^?]+\\?[BbQq]\\?[^?]+\\?=)");
    private static final Pattern Q_ENCODING_PATTERN = Pattern.compile(
            "=\\?[^?]+\\?[Qq]\\?[^?]+\\?=");
    private static final Pattern BASE64_BLOCK_PATTERN = Pattern.compile(
            "=\\?([^?]+)\\?[Bb]\\?([^?]+)\\?=");

    /**
     * Decodes a string containing RFC 2047 encoded words,
     * even if they are
     * <li> folded
     * <li> embedded without space before or after
     * <li> with spaces in Q-blocs
     * <li> with wrong lenght B-blocs.
     *
     * @param input raw header value possibly containing folded encoded words
     * @return the decoded string
     */
    public static String decodeRfc2047Flexible(String input) {
        if (input == null || input.isEmpty()) return input;

        // unfold
        input = input.replaceAll("\\r?\\n[ \t]*", "");

        // add encoded bloc end if needed
        if (ENCODED_WORD_PATTERN_BEGIN.matcher(input).find() && !input.trim().endsWith("?="))
            input+="?=";

        // Split the encoded blocs even without spaces
        Matcher matcher = ENCODED_WORD_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String encoded = matcher.group(1);
            try {
                // Encode space and remove tab in quote-encoded bloc
                if (Q_ENCODING_PATTERN.matcher(encoded).matches()) {
                    encoded = encoded.replace(" ", "_");
                    encoded = encoded.replace("\t", "");
                } else {
                    // Pad the Base64 encoded string with '=' characters so that its length is a multiple of 4.
                    Matcher base64matcher = BASE64_BLOCK_PATTERN.matcher(encoded);
                    if (base64matcher.matches()) {
                        String charset = base64matcher.group(1);
                        String encodedText = base64matcher.group(2);
                        int mod = encodedText.length() % 4;
                        if (mod != 0) {
                            int missing = 4 - mod;
                            encodedText = encodedText + "=".repeat(missing);
                        }
                        encoded = "=?" + charset + "?B?" + encodedText + "?=";
                    }
                }
                String decoded = MimeUtility.decodeText(encoded);
                matcher.appendReplacement(result, Matcher.quoteReplacement(decoded));
            } catch (UnsupportedEncodingException e) {
                // Fallback: keep the original block if decoding fails
                matcher.appendReplacement(result, Matcher.quoteReplacement(encoded));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static final Pattern SIMPLE_EMAIL_WITH_NAME_PATTERN = Pattern.compile(
            "^\\s*.*?<\\s*[a-zA-Z0-9](?:[a-zA-Z0-9._%+-]{0,62}[a-zA-Z0-9])?@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\s*>\\s*$"
    );

    /**
     * Removes invalid email addresses and duplicates from a given list of email addresses.
     *
     * Filters out email addresses that do not match a pre-defined valid pattern and removes duplicate email
     * addresses while preserving the original order of the list.
     *
     * @param addressList the input list of email addresses to process; may be null
     * @return a new list of valid and unique email addresses. If the input list is null,
     *         an empty list is returned. If no valid email addresses are found, the original list is returned.
     */
    public static List<String> removeInvalidAndDuplicatesFromAddressesList(List<String> addressList) {
        if (addressList == null) return Collections.emptyList();
        if (addressList.size() <= 1) return addressList;

        List<String> decodedList = new ArrayList<>();
        for (String address : addressList) {
            if (SIMPLE_EMAIL_WITH_NAME_PATTERN.matcher(address).matches())
                decodedList.add(address.toLowerCase().trim());
        }

        if (decodedList.isEmpty())
            decodedList=addressList;

        return new ArrayList<>(new LinkedHashSet<>(decodedList));
    }
}
