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
/**
 * This is copied from Tika 1.17
 * under http://www.apache.org/licenses/LICENSE-2.0
 *
 * Conform with MSDN directives to de-encapsulate HTML from RTF
 * https://msdn.microsoft.com/en-us/library/ee159984(v=exchg.80).aspx
 */

package fr.gouv.vitam.tools.mailextractlib.formattools.rtf;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Locale.ENGLISH;

/**
 * The Class CharsetUtils for RTF used charsets decoding.
 */
public class CharsetUtils {

    private static final Pattern CHARSET_NAME_PATTERN =
            Pattern.compile("[ \\\"]*([^ >,;\\\"]+).*");

    private static final Pattern ISO_NAME_PATTERN =
            Pattern.compile(".*8859-(\\d+)");

    private static final Pattern CP_NAME_PATTERN =
            Pattern.compile("cp-(\\d+)");

    private static final Pattern WIN_NAME_PATTERN =
            Pattern.compile("win-?(\\d+)");

    private static final Map<String, Charset> COMMON_CHARSETS =
            new HashMap<String, Charset>();

    private static Method getCharsetICU = null;
    private static Method isSupportedICU = null;

    private static Map<String, Charset> initCommonCharsets(String... names) {
        Map<String, Charset> charsets = new HashMap<String, Charset>();
        for (String name : names) {
            try {
                Charset charset = Charset.forName(name);
                COMMON_CHARSETS.put(name.toLowerCase(ENGLISH), charset);
                for (String alias : charset.aliases()) {
                    COMMON_CHARSETS.put(alias.toLowerCase(ENGLISH), charset);
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return charsets;
    }

    static {
        initCommonCharsets(
                "Big5",
                "EUC-JP", "EUC-KR", "x-EUC-TW",
                "GB18030",
                "IBM855", "IBM866",
                "ISO-2022-CN", "ISO-2022-JP", "ISO-2022-KR",
                "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4",
                "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8",
                "ISO-8859-9", "ISO-8859-11", "ISO-8859-13", "ISO-8859-15",
                "KOI8-R",
                "x-MacCyrillic",
                "SHIFT_JIS",
                "UTF-8", "UTF-16BE", "UTF-16LE",
                "windows-1251", "windows-1252", "windows-1253", "windows-1255");

        // Common aliases/typos not included in standard charset definitions
        COMMON_CHARSETS.put("iso-8851-1", COMMON_CHARSETS.get("iso-8859-1"));
        COMMON_CHARSETS.put("windows", COMMON_CHARSETS.get("windows-1252"));
        COMMON_CHARSETS.put("koi8r", COMMON_CHARSETS.get("koi8-r"));

        // See if we can load the icu4j CharsetICU class
        Class<?> icuCharset = null;
        try {
            icuCharset = CharsetUtils.class.getClassLoader().loadClass(
                    "com.ibm.icu.charset.CharsetICU");
        } catch (ClassNotFoundException ignored) {
        }
        if (icuCharset != null) {
            try {
                getCharsetICU = icuCharset.getMethod("forNameICU", String.class);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            try {
                isSupportedICU = icuCharset.getMethod("isSupported", String.class);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Safely return whether &lt;charsetName&gt; is supported, without throwing
     * exceptions.
     *
     * @param charsetName
     *            Name of charset (can be null)
     * @return true if the character set is supported
     */
    public static boolean isSupported(String charsetName) {
        try {
            if (isSupportedICU != null && ((Boolean) isSupportedICU.invoke(null, charsetName)).booleanValue()) {
                return true;
            }
            return Charset.isSupported(charsetName);
        } catch (IllegalCharsetNameException e) {
            return false;
        } catch (IllegalArgumentException e) {
            // null, for example
            return false;
        } catch (Exception e) {
            // Unexpected exception, what to do?
            return false;
        }
    }

    /**
     * Handle various common charset name errors, and return something that will
     * be considered valid (and is normalized).
     *
     * @param charsetName
     *            name of charset to process
     * @return potentially remapped/cleaned up version of charset name
     */
    public static String clean(String charsetName) {
        try {
            return forName(charsetName).name();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns Charset impl, if one exists. This method optionally uses ICU4J's
     * CharsetICU.forNameICU, if it is found on the classpath, else only uses
     * JDK's builtin Charset.forName.
     *
     * @param name
     *            the name
     * @return the charset
     */
    public static Charset forName(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }

        // Get rid of cruft around names, like <>, trailing commas, etc.
        Matcher m = CHARSET_NAME_PATTERN.matcher(name);
        if (!m.matches()) {
            throw new IllegalCharsetNameException(name);
        }
        name = m.group(1);

        String lower = name.toLowerCase(Locale.ENGLISH);
        Charset charset = COMMON_CHARSETS.get(lower);
        if (charset != null) {
            return charset;
        } else if ("none".equals(lower) || "no".equals(lower)) {
            throw new IllegalCharsetNameException(name);
        } else {
            Matcher iso = ISO_NAME_PATTERN.matcher(lower);
            Matcher cp = CP_NAME_PATTERN.matcher(lower);
            Matcher win = WIN_NAME_PATTERN.matcher(lower);
            if (iso.matches()) {
                // Handle "iso 8859-x" error
                name = "iso-8859-" + iso.group(1);
                charset = COMMON_CHARSETS.get(name);
            } else if (cp.matches()) {
                // Handle "cp-xxx" error
                name = "cp" + cp.group(1);
                charset = COMMON_CHARSETS.get(name);
            } else if (win.matches()) {
                // Handle "winxxx" and "win-xxx" errors
                name = "windows-" + win.group(1);
                charset = COMMON_CHARSETS.get(name);
            }
            if (charset != null) {
                return charset;
            }
        }

        if (getCharsetICU != null) {
            try {
                Charset cs = (Charset) getCharsetICU.invoke(null, name);
                if (cs != null) {
                    return cs;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return Charset.forName(name);
    }
}
