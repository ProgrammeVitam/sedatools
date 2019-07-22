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


package fr.gouv.vitam.tools.mailextractlib.formattools.rtf;

import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class HTMLFromRTFextractor for desencapsulating text and html from rtf.
 * <p>
 * This is strongly inspired by RTFParser in Tika 1.17
 * under http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Conform with MSDN directives to de-encapsulate HTML from RTF
 * https://msdn.microsoft.com/en-us/library/ee159984(v=exchg.80).aspx
 */
public class HTMLFromRTFExtractor {

    // Input management
    private PushbackInputStream pbis;

    // Charset fields
    private CharsetDecoder decoder;
    private Charset globalCharset;
    private Charset globalLastCharset;
    private int globalDefaultFont;
    private int globalCurFontID;

    // Parsing state fields
    private boolean globalInHeader;
    private RTFGroupState globalGroupState;
    private int globalAnsiSkip; // After seeing a unicode escape we must skip
    // the next
    // ucSkip ansi chars (the "unicode shadow")

    // Output management
    private StringBuilder resultBuilder;
    private String resultString;
    // Hold pending bytes (encoded in the current charset)
    // for text output:
    private byte[] pendingBytes = new byte[16];
    private int pendingByteCount;
    private ByteBuffer pendingByteBuffer;
    // Used when we decode bytes -> chars using CharsetDecoder:
    private char[] outputArray;
    private CharBuffer outputBuffer;
    // Holds pending chars for text output
    private char[] pendingChars = new char[10];
    private int pendingCharCount;

    // type boolean
    private boolean isText;
    private boolean isHTML;

    // constructors

    /**
     * Instantiates a new HTML from RTF extractor.
     *
     * @param is the is
     */
    public HTMLFromRTFExtractor(InputStream is) {
        pbis = new PushbackInputStream(is, 100);
        initHTMLFromRTFExtractor();
    }

    /**
     * Instantiates a new HTML from RTF extractor.
     *
     * @param rtfText the rtf text
     */
    public HTMLFromRTFExtractor(String rtfText) {
        ByteArrayInputStream bais;

        bais = new ByteArrayInputStream(rtfText.getBytes(StandardCharsets.UTF_8));
        pbis = new PushbackInputStream(bais, 100);
        initHTMLFromRTFExtractor();
        detectEncapsulatedHTMLorTEXTinRTF();
    }

    private void initHTMLFromRTFExtractor() {
        // Charset fields
        decoder = null;
        globalCharset = WINDOWS_1252;
        globalLastCharset = null;
        globalDefaultFont = -1;
        globalCurFontID = 0;

        // Parsing state fields
        globalInHeader = true;
        globalGroupState = null;
        globalAnsiSkip = 0;

        // Output management
        resultBuilder = null;
        pendingBytes = new byte[16];
        pendingByteCount = 0;
        pendingByteBuffer = ByteBuffer.wrap(pendingBytes);
        outputArray = new char[128];
        outputBuffer = CharBuffer.wrap(outputArray);
        pendingChars = new char[10];
        pendingCharCount = 0;
    }

    // Charset management according rtf rules

    private static Charset getCharset(String name) {
        try {
            return CharsetUtils.forName(name);
        } catch (Exception e) {
            return ASCII;
        }
    }

    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final Charset WINDOWS_1252 = getCharset("WINDOWS-1252");
    private static final Charset MAC_ROMAN = getCharset("MacRoman");
    private static final Charset SHIFT_JIS = getCharset("Shift_JIS");
    private static final Charset WINDOWS_57011 = getCharset("windows-57011");
    private static final Charset WINDOWS_57010 = getCharset("windows-57010");
    private static final Charset WINDOWS_57009 = getCharset("windows-57009");
    private static final Charset WINDOWS_57008 = getCharset("windows-57008");
    private static final Charset WINDOWS_57007 = getCharset("windows-57007");
    private static final Charset WINDOWS_57006 = getCharset("windows-57006");
    private static final Charset WINDOWS_57005 = getCharset("windows-57005");
    private static final Charset WINDOWS_57004 = getCharset("windows-57004");
    private static final Charset WINDOWS_57003 = getCharset("windows-57003");
    private static final Charset X_ISCII91 = getCharset("x-ISCII91");
    private static final Charset X_MAC_CENTRAL_EUROPE = getCharset("x-MacCentralEurope");
    private static final Charset MAC_CYRILLIC = getCharset("MacCyrillic");
    private static final Charset X_JOHAB = getCharset("x-Johab");
    private static final Charset CP12582 = getCharset("CP1258");
    private static final Charset CP12572 = getCharset("CP1257");
    private static final Charset CP12562 = getCharset("CP1256");
    private static final Charset CP12552 = getCharset("CP1255");
    private static final Charset CP12542 = getCharset("CP1254");
    private static final Charset CP12532 = getCharset("CP1253");
    private static final Charset CP1252 = getCharset("CP1252");
    private static final Charset CP12512 = getCharset("CP1251");
    private static final Charset CP12502 = getCharset("CP1250");
    private static final Charset CP950 = getCharset("CP950");
    private static final Charset CP949 = getCharset("CP949");
    private static final Charset MS9362 = getCharset("MS936");
    private static final Charset MS8742 = getCharset("MS874");
    private static final Charset CP866 = getCharset("CP866");
    private static final Charset CP865 = getCharset("CP865");
    private static final Charset CP864 = getCharset("CP864");
    private static final Charset CP863 = getCharset("CP863");
    private static final Charset CP862 = getCharset("CP862");
    private static final Charset CP860 = getCharset("CP860");
    private static final Charset CP852 = getCharset("CP852");
    private static final Charset CP8502 = getCharset("CP850");
    private static final Charset CP819 = getCharset("CP819");
    private static final Charset WINDOWS_720 = getCharset("windows-720");
    private static final Charset WINDOWS_711 = getCharset("windows-711");
    private static final Charset WINDOWS_710 = getCharset("windows-710");
    private static final Charset WINDOWS_709 = getCharset("windows-709");
    private static final Charset ISO_8859_6 = getCharset("ISO-8859-6");
    private static final Charset CP4372 = getCharset("CP437");
    private static final Charset CP850 = getCharset("cp850");
    private static final Charset CP437 = getCharset("cp437");
    private static final Charset MS874 = getCharset("ms874");
    private static final Charset CP1257 = getCharset("cp1257");
    private static final Charset CP1256 = getCharset("cp1256");
    private static final Charset CP1255 = getCharset("cp1255");
    private static final Charset CP1258 = getCharset("cp1258");
    private static final Charset CP1254 = getCharset("cp1254");
    private static final Charset CP1253 = getCharset("cp1253");
    private static final Charset MS950 = getCharset("ms950");
    private static final Charset MS936 = getCharset("ms936");
    private static final Charset MS1361 = getCharset("ms1361");
    private static final Charset MS932 = getCharset("MS932");
    private static final Charset CP1251 = getCharset("cp1251");
    private static final Charset CP1250 = getCharset("cp1250");
    private static final Charset MAC_THAI = getCharset("MacThai");
    private static final Charset MAC_TURKISH = getCharset("MacTurkish");
    private static final Charset MAC_GREEK = getCharset("MacGreek");
    private static final Charset MAC_ARABIC = getCharset("MacArabic");
    private static final Charset MAC_HEBREW = getCharset("MacHebrew");
    private static final Charset JOHAB = getCharset("johab");
    private static final Charset BIG5 = getCharset("Big5");
    private static final Charset GB2312 = getCharset("GB2312");
    private static final Charset MS949 = getCharset("ms949");

    // The RTF doc has a "font table" that assigns ords
    // (f0, f1, f2, etc.) to fonts and charsets, using the
    // \fcharsetN control word. This mapping maps from the
    // N to corresponding Java charset:
    private static final Map<Integer, Charset> FCHARSET_MAP = new HashMap<Integer, Charset>();
    // The RTF may specify the \ansicpgN charset in the
    // header; this maps the N to the corresponding Java
    // character set:
    private static final Map<Integer, Charset> ANSICPG_MAP = new HashMap<Integer, Charset>();

    static {
        FCHARSET_MAP.put(0, WINDOWS_1252); // ANSI
        // charset 1 is Default
        // charset 2 is Symbol

        FCHARSET_MAP.put(77, MAC_ROMAN); // Mac Roman
        FCHARSET_MAP.put(78, SHIFT_JIS); // Mac Shift Jis
        FCHARSET_MAP.put(79, MS949); // Mac Hangul
        FCHARSET_MAP.put(80, GB2312); // Mac GB2312
        FCHARSET_MAP.put(81, BIG5); // Mac Big5
        FCHARSET_MAP.put(82, JOHAB); // Mac Johab (old)
        FCHARSET_MAP.put(83, MAC_HEBREW); // Mac Hebrew
        FCHARSET_MAP.put(84, MAC_ARABIC); // Mac Arabic
        FCHARSET_MAP.put(85, MAC_GREEK); // Mac Greek
        FCHARSET_MAP.put(86, MAC_TURKISH); // Mac Turkish
        FCHARSET_MAP.put(87, MAC_THAI); // Mac Thai
        FCHARSET_MAP.put(88, CP1250); // Mac East Europe
        FCHARSET_MAP.put(89, CP1251); // Mac Russian

        FCHARSET_MAP.put(128, MS932); // Shift JIS
        FCHARSET_MAP.put(129, MS949); // Hangul
        FCHARSET_MAP.put(130, MS1361); // Johab
        FCHARSET_MAP.put(134, MS936); // GB2312
        FCHARSET_MAP.put(136, MS950); // Big5
        FCHARSET_MAP.put(161, CP1253); // Greek
        FCHARSET_MAP.put(162, CP1254); // Turkish
        FCHARSET_MAP.put(163, CP1258); // Vietnamese
        FCHARSET_MAP.put(177, CP1255); // Hebrew
        FCHARSET_MAP.put(178, CP1256); // Arabic
        // FCHARSET_MAP.put( 179, "" ); // Arabic Traditional
        // FCHARSET_MAP.put( 180, "" ); // Arabic user
        // FCHARSET_MAP.put( 181, "" ); // Hebrew user
        FCHARSET_MAP.put(186, CP1257); // Baltic

        FCHARSET_MAP.put(204, CP1251); // Russian
        FCHARSET_MAP.put(222, MS874); // Thai
        FCHARSET_MAP.put(238, CP1250); // Eastern European
        FCHARSET_MAP.put(254, CP437); // PC 437
        FCHARSET_MAP.put(255, CP850); // OEM
    }

    static {
        ANSICPG_MAP.put(437, CP4372); // US IBM
        ANSICPG_MAP.put(708, ISO_8859_6); // Arabic (ASMO 708)

        ANSICPG_MAP.put(709, WINDOWS_709); // Arabic (ASMO 449+, BCON V4)
        ANSICPG_MAP.put(710, WINDOWS_710); // Arabic (transparent Arabic)

        ANSICPG_MAP.put(819, CP819); // Windows 3.1 (US & Western Europe)
        ANSICPG_MAP.put(850, CP8502); // IBM Multilingual
        ANSICPG_MAP.put(852, CP852); // Eastern European
        ANSICPG_MAP.put(860, CP860); // Portuguese
        ANSICPG_MAP.put(862, CP862); // Hebrew
        ANSICPG_MAP.put(863, CP863); // French Canadian
        ANSICPG_MAP.put(864, CP864); // Arabic
        ANSICPG_MAP.put(865, CP865); // Norwegian
        ANSICPG_MAP.put(866, CP866); // Soviet Union
        ANSICPG_MAP.put(874, MS8742); // Thai
        ANSICPG_MAP.put(932, MS932); // Japanese
        ANSICPG_MAP.put(936, MS9362); // Simplified Chinese
        ANSICPG_MAP.put(949, CP949); // Korean
        ANSICPG_MAP.put(950, CP950); // Traditional Chinese
        ANSICPG_MAP.put(1250, CP12502); // Eastern European
        ANSICPG_MAP.put(1251, CP12512); // Cyrillic
        ANSICPG_MAP.put(1252, CP1252); // Western European
        ANSICPG_MAP.put(1253, CP12532); // Greek
        ANSICPG_MAP.put(1254, CP12542); // Turkish
        ANSICPG_MAP.put(1255, CP12552); // Hebrew
        ANSICPG_MAP.put(1256, CP12562); // Arabic
        ANSICPG_MAP.put(1257, CP12572); // Baltic
        ANSICPG_MAP.put(1258, CP12582); // Vietnamese
        ANSICPG_MAP.put(1361, X_JOHAB); // Johab
        ANSICPG_MAP.put(10000, MAC_ROMAN); // Mac Roman
        ANSICPG_MAP.put(10001, SHIFT_JIS); // Mac Japan
        ANSICPG_MAP.put(10004, MAC_ARABIC); // Mac Arabic
        ANSICPG_MAP.put(10005, MAC_HEBREW); // Mac Hebrew
        ANSICPG_MAP.put(10006, MAC_GREEK); // Mac Hebrew
        ANSICPG_MAP.put(10007, MAC_CYRILLIC); // Mac Cyrillic
        ANSICPG_MAP.put(10029, X_MAC_CENTRAL_EUROPE); // MAC Latin2
        ANSICPG_MAP.put(10081, MAC_TURKISH); // Mac Turkish
        ANSICPG_MAP.put(57002, X_ISCII91); // Devanagari

        // in theory these other charsets are simple
        // shifts off of Devanagari, so we could impl that
        // here:
        ANSICPG_MAP.put(57003, WINDOWS_57003); // Bengali
        ANSICPG_MAP.put(57004, WINDOWS_57004); // Tamil
        ANSICPG_MAP.put(57005, WINDOWS_57005); // Telugu
        ANSICPG_MAP.put(57006, WINDOWS_57006); // Assamese
        ANSICPG_MAP.put(57007, WINDOWS_57007); // Oriya
        ANSICPG_MAP.put(57008, WINDOWS_57008); // Kannada
        ANSICPG_MAP.put(57009, WINDOWS_57009); // Malayalam
        ANSICPG_MAP.put(57010, WINDOWS_57010); // Gujariti
        ANSICPG_MAP.put(57011, WINDOWS_57011); // Punjabi
    }

    // Holds the font table from this RTF doc, mapping
    // the font number (from \fN control word) to the
    // corresponding charset:
    private final Map<Integer, Charset> fontToCharset = new HashMap<Integer, Charset>();

    // Parsing functions

    // Main parsing function
    private void doExtract() throws IOException {
        while (true) {
            final int b = pbis.read();
            if (b == -1) {
                pushText();
                break;
            } else if (b == '\\') {
                parseControlToken();
            } else if (b == '{') {
                pushText();
                processGroupStart();
            } else if (b == '}') {
                pushText();
                processGroupEnd();
                if (globalGroupState == null) {
                    // parsed document closing brace
                    break;
                }
            } else if (b != '\r' && b != '\n' && !globalGroupState.ignore) {
                // Linefeed and carriage return are not significant
                if (globalAnsiSkip != 0) {
                    globalAnsiSkip--;
                } else {
                    addOutputByte(b);
                }
            }
        }
    }

    // Push new GroupState
    private void processGroupStart() throws IOException {
        globalAnsiSkip = 0;

        // Make new GroupState
        globalGroupState = new RTFGroupState(globalGroupState);

        // Check for ignorable groups. Note that
        // sometimes we un-ignore within this group, eg
        // when handling upr escape.
        int b2 = pbis.read();
        if (b2 == '\\') {
            int b3 = pbis.read();
            if (b3 == '*') {
                globalGroupState.setInIgnorable(true);
            }
            pbis.unread(b3);
        }
        pbis.unread(b2);
    }

    // Pop current GroupState
    private void processGroupEnd() {
        // delete any output till in header
        if (globalInHeader)
            resultBuilder.setLength(0);

        globalAnsiSkip = 0;
        // Restore group state:
        globalGroupState = globalGroupState.getPreviousGroupState();

    }

    // Parse a rtf control token (\...)
    private void parseControlToken() throws IOException {
        int b = pbis.read();
        if (b == '\'') {
            // escaped hex char
            parseHexChar();
        } else if (isAlpha(b)) {
            // control word
            parseControlWord((char) b);
        } else if ((b == '{' || b == '}' || b == '\\' || b == '\r' || b == '\n') && !globalGroupState.ignore) {
            // escaped char
            addOutputByte(b);
        } else if (b != -1) {
            // control symbol, eg \* or \~
            processControlSymbol((char) b);
        }
    }

    // Parse a rtf hex escaped character (\'XX)
    private void parseHexChar() throws IOException {
        int hex1 = pbis.read();
        if (!isHexChar(hex1)) {
            // DOC ERROR (malformed hex escape): ignore
            pbis.unread(hex1);
            return;
        }

        int hex2 = pbis.read();
        if (!isHexChar(hex2)) {
            // DOC ERROR (malformed hex escape):
            // ignore
            pbis.unread(hex2);
            return;
        }

        if (globalAnsiSkip != 0) {
            // Skip this ansi char since we are
            // still in the shadow of a unicode
            // escape:
            globalAnsiSkip--;
        } else {
            if (!globalGroupState.ignore)
                // Unescape:
                addOutputByte(16 * hexValue(hex1) + hexValue(hex2));
        }
    }

    // Parse a rtf control word (\... not escaped character)
    private void parseControlWord(int firstChar) throws IOException {
        StringBuilder controlWord = new StringBuilder();
        ;

        controlWord.append((char) firstChar);
        int b = pbis.read();
        while (isAlpha(b)) {
            controlWord.append((char) b);
            b = pbis.read();
        }

        boolean hasParam = false;
        boolean negParam = false;
        if (b == '-') {
            negParam = true;
            hasParam = true;
            b = pbis.read();
        }

        int param = 0;
        while (isDigit(b)) {
            param *= 10;
            param += (b - '0');
            hasParam = true;
            b = pbis.read();
        }

        // space is consumed as part of the
        // control word, but is not added to the control word
        if (b != ' ') {
            pbis.unread(b);
        }

        if (hasParam) {
            if (negParam) {
                param = -param;
            }
            processControlWord(controlWord.toString(), param);
        } else
            processNoParamControlWord(controlWord.toString());

    }

    // Handle non-parameter control word
    private void processNoParamControlWord(String controlWord) {
        if (globalInHeader) {
            switch (controlWord) {
                case "ansi":
                    globalCharset = WINDOWS_1252;
                    break;
                case "pca":
                    globalCharset = CP850;
                    break;
                case "pc":
                    globalCharset = CP437;
                    break;
                case "mac":
                    globalCharset = MAC_ROMAN;
                    break;
                case "par":
                case "pard":
                case "sect":
                case "sectd":
                case "plain":
                case "ltrch":
                case "rtlch":
                    globalInHeader = false;
                default:
                    // ignore others colortbl stylesheet fonttbl listtable
                    // listoverridetable
                    break;
            }
        }

        // controlWord to handle if not in htlmref or an ignorable group
        // different from htmltag
        // get rid of pard, plain, shptxt, atnid, atnauthor, annotation,
        // listtext, cell, sp, sn, sv, object, objdata, pict,...
        if (!globalGroupState.ignore) {
            switch (controlWord) {
                case "par":
                case "line":
                    addOutputChar('\r');
                    addOutputChar('\n');
                    break;
                case "tab":
                    addOutputChar('\t');
                    break;
                case "htmltag":
                    pushText();
                    globalGroupState.setInHtmltag(true);
                    break;
                case "htmlrtf":
                    pushText();
                    globalGroupState.setInHtmlrtf(true);
                    break;
            }
        }
    }

    // Handle control word that takes a parameter
    private void processControlWord(String controlWord, int param) {
        Charset cs;
        // TODO: afN? (associated font number)

        if (globalInHeader) {
            switch (controlWord) {
                case "ansicpg":
                    cs = ANSICPG_MAP.get(param);
                    if (cs != null) {
                        globalCharset = cs;
                    }
                    break;
                case "f":
                    globalCurFontID = param;
                    break;
                case "deff":
                    globalDefaultFont = param;
                    break;
                case "fcharset":
                    cs = FCHARSET_MAP.get(param);
                    if (cs != null) {
                        fontToCharset.put(globalCurFontID, cs);
                    }
                    break;
            }
        } else {
            // In document
            // specific code to get html
            switch (controlWord) {
                case "htmltag":
                    pushText();
                    globalGroupState.setInHtmltag(true);
                    break;
                case "htmlrtf":
                    pushText();
                    if (param == 0)
                        globalGroupState.setInHtmlrtf(false);
                    else
                        globalGroupState.setInHtmlrtf(true);
                    break;
                case "f":
                    // Change current font
                    Charset fontCharset = fontToCharset.get(param);
                    // Push any buffered text before changing
                    // font:
                    pushText();
                    if (fontCharset != null) {
                        globalGroupState.fontCharset = fontCharset;
                    } else {
                        // DOC ERROR: font change referenced a
                        // non-table'd font number
                        globalGroupState.fontCharset = null;
                    }
                    break;
                // Process unicode escape. This can appear in doc
                // or in header, since the metadata (info) fields
                // in the header can be unicode escaped as well:
                case "u":
                    // Unicode escape
                    if (!globalGroupState.ignore) {
                        final char utf16CodeUnit = (char) (param & 0xffff);
                        addOutputChar(utf16CodeUnit);
                    }
                    // After seeing a unicode escape we must
                    // skip the next ucSkip ansi chars (the
                    // "unicode shadow")
                    globalAnsiSkip = globalGroupState.ucSkip;
                    break;
                case "uc":
                    globalGroupState.ucSkip = param;
                    break;
            }
        }
    }

    private void processControlSymbol(char ch) {
        switch (ch) {
            case '~':
                // Non-breaking space -> unicode NON-BREAKING SPACE
                addOutputChar('\u00a0');
                break;
            case '*':
                // Ignorable destination (control words defined after
                // the 1987 RTF spec). These are already handled by
                // processGroupStart()
                break;
            case '-':
                // Optional hyphen -> unicode SOFT HYPHEN
                addOutputChar('\u00ad');
                break;
            case '_':
                // Non-breaking hyphen -> unicode NON-BREAKING HYPHEN
                addOutputChar('\u2011');
                break;
            default:
                break;
        }
    }

    /**
     * Checks if is hex char.
     *
     * @param ch the ch
     * @return true, if is hex char
     */
    protected static boolean isHexChar(int ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private static boolean isAlpha(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Hex value.
     *
     * @param ch the ch
     * @return the int
     */
    protected static int hexValue(int ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        } else if (ch >= 'a' && ch <= 'z') {
            return 10 + (ch - 'a');
        } else {
            assert ch >= 'A' && ch <= 'Z';
            return 10 + (ch - 'A');
        }
    }

    // Output management with all charset treatment

    // Buffers the byte (unit in the current charset) for
    // output:
    private void addOutputByte(int b) {
        assert b >= 0 && b < 256 : "byte value out of range: " + b;

        if (pendingCharCount != 0) {
            pushPendingChars();
        }
        // Save the byte in pending buffer:
        if (pendingByteCount == pendingBytes.length) {
            // Gradual but exponential growth:
            final byte[] newArray = new byte[(int) (pendingBytes.length * 1.25)];
            System.arraycopy(pendingBytes, 0, newArray, 0, pendingBytes.length);
            pendingBytes = newArray;
            pendingByteBuffer = ByteBuffer.wrap(pendingBytes);
        }
        pendingBytes[pendingByteCount++] = (byte) b;
    }

    // Buffers a UTF16 code unit for output
    private void addOutputChar(char ch) {
        if (pendingByteCount != 0) {
            pushPendingBytes();
        }
        // Save the char in pending buffer:
        if (pendingCharCount == pendingChars.length) {
            // Gradual but exponential growth:
            final char[] newArray = new char[(int) (pendingChars.length * 1.25)];
            System.arraycopy(pendingChars, 0, newArray, 0, pendingChars.length);
            pendingChars = newArray;
        }
        pendingChars[pendingCharCount++] = ch;
    }

    // Decodes the buffered bytes in pendingBytes
    // into UTF16 code units, and sends the characters
    // to the out ContentHandler, if we are in the body,
    // else appends the characters to the pendingBuffer
    private void pushPendingBytes() {
        if (pendingByteCount > 0 && !globalGroupState.ignore) {

            final CharsetDecoder decoder = getDecoder();
            pendingByteBuffer.limit(pendingByteCount);
            assert pendingByteBuffer.position() == 0;
            assert outputBuffer.position() == 0;

            while (true) {
                // We pass true for endOfInput because, when
                // we are called, we should have seen a
                // complete sequence of characters for this
                // charset:
                final CoderResult result = decoder.decode(pendingByteBuffer, outputBuffer, true);

                final int pos = outputBuffer.position();
                if (pos > 0) {
                    resultBuilder.append(outputArray, 0, pos);
                    outputBuffer.position(0);
                }

                if (result == CoderResult.UNDERFLOW) {
                    break;
                }
            }

            while (true) {
                final CoderResult result = decoder.flush(outputBuffer);

                final int pos = outputBuffer.position();
                if (pos > 0) {
                    resultBuilder.append(outputArray, 0, pos);
                    outputBuffer.position(0);
                }

                if (result == CoderResult.UNDERFLOW) {
                    break;
                }
            }

            // Reset for next decode
            decoder.reset();
            pendingByteBuffer.position(0);
        }

        pendingByteCount = 0;
    }

    private CharsetDecoder getDecoder() {
        Charset charset = getCharset();

        // Common case: charset is same as last time, so
        // just reuse it:
        if (globalLastCharset == null || !charset.equals(globalLastCharset)) {
            decoder = charset.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            globalLastCharset = charset;
        }

        return decoder;
    }

    // Return current charset in-use
    private Charset getCharset() {
        // If a specific font (fN) was set, use its charset
        if (globalGroupState.fontCharset != null) {
            return globalGroupState.fontCharset;
        }
        // Else, if global default font (defN) was set, use that one
        if (globalDefaultFont != -1 && !globalInHeader) {
            Charset cs = fontToCharset.get(globalDefaultFont);
            if (cs != null) {
                return cs;
            }
        }

        return globalCharset;
    }

    // Push pending UTF16 units to out ContentHandler
    private void pushPendingChars() {
        if (pendingCharCount != 0) {
            resultBuilder.append(pendingChars, 0, pendingCharCount);
            pendingCharCount = 0;
        }
    }

    // Push pending bytes or pending chars:
    private void pushText() {
        if (pendingByteCount != 0) {
            assert pendingCharCount == 0;
            pushPendingBytes();
        } else {
            pushPendingChars();
        }
    }

    private boolean detectEncapsulatedHTMLorTEXTinRTF() {
        byte[] buf = new byte[100];
        String test;
        int len = 0, inc;

        try {
            while (len < 100) {
                inc = pbis.read(buf, len, 100 - len);
                if (inc == -1)
                    return false;
                len += inc;
            }
            test = new String(buf);
            pbis.unread(buf, 0, len);
        } catch (IOException e) {
            return false;
        }

        if (test.indexOf("\\fromhtml") >= 0) {
            isHTML = true;
            return true;
        }
        if (test.indexOf("\\fromtext") >= 0) {
            isText = true;
            return true;
        } else
            return false;
    }

    // public functions

    /**
     * Checks if is encapsulated HTML in RTF.
     *
     * @return true, if is encapsulated HTML in RTF
     */
    public boolean isEncapsulatedHTMLinRTF() {
        return isHTML;
    }

    /**
     * Checks if is encapsulated TEXT in RTF.
     *
     * @return true, if is encapsulated TEXT in RTF
     */
    public boolean isEncapsulatedTEXTinRTF() {
        return isText;
    }

    /**
     * Get the de-encapsulate HTML from the RTF source.
     *
     * @return the string
     * @throws MailExtractLibException the extraction exception
     */
    public String getDeEncapsulateHTMLFromRTF() throws MailExtractLibException {
        if (resultString == null) {
            resultBuilder = new StringBuilder();
            try {
                doExtract();
            } catch (IOException e) {
                throw new MailExtractLibException("Can't extract html from rtf", e);
            }
            resultString = resultBuilder.toString();

            if (isHTML) {
                Matcher m = Pattern.compile("<(h|H)(e|E)(a|A)(d|D).*>").matcher(resultString);
                if (m.find()) {
                    // add a meta tag to say it's UTF-8 just after <head> tag
                    resultString = resultBuilder
                            .insert(m.end(),
                                    "\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">")
                            .toString();
                } else {
                    // add a meta tag to say it's UTF-8 at the beginning (a bit
                    // trash)
                    resultString = resultBuilder
                            .insert(0, "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n")
                            .toString();
                }
            }

            resultBuilder = null;
        }

        return resultString;
    }

}
