package fr.gouv.vitam.tools.mailextractlib;

import fr.gouv.vitam.tools.mailextractlib.store.javamail.charsets.OtherMimeCharsetProvider;
import fr.gouv.vitam.tools.mailextractlib.utils.HtmlAndXmlEscape;
import fr.gouv.vitam.tools.mailextractlib.utils.MailExtractLibException;
import fr.gouv.vitam.tools.mailextractlib.utils.RFC822Headers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class TestUtilities implements AllTests{
    @Test
    public void testRFC2047() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testUtilities");
        String qencoded = "Notification \n" +
                "\td'=?unicode-1-1-utf-7?Q?+AOk-tat \n" +
                "\tde \n" +
                "\tremise \n" +
                "\t(+AOk-chec)?=";
        String bencoded ="=?iso-8859-1?B?Tm90aWZpY2F0aW9uIGQn6XRhdCBkZSByZW1pc2UgKOljaGVjKQ?=";

        OtherMimeCharsetProvider otm=new OtherMimeCharsetProvider();
        //when
        String qdecoded= RFC822Headers.decodeRfc2047Flexible(qencoded);
        String bdecoded= RFC822Headers.decodeRfc2047Flexible(bencoded);

        //then
        assertThat(qdecoded).isEqualTo("Notification d'état de remise (échec)");
        assertThat(bdecoded).isEqualTo("Notification d'état de remise (échec)");
    }

    @Test
    public void testCharsets() throws MailExtractLibException, InterruptedException, IOException {
        //given
        AllTests.initializeTests("testCharsets");

        OtherMimeCharsetProvider otm=new OtherMimeCharsetProvider();
        //when/then
        assertThatCode(() -> {
            Charset cs=Charset.forName("'us-ascii");
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            Charset cs=Charset.forName("'usascii");
        }).isInstanceOf(IllegalCharsetNameException.class);

    }

    // Full HTML input string for tests
    private static final String HTML_INPUT =
            // Named character entities
            // Basic XML symbols
            "&lt;&gt;&amp;&quot;&apos;" +

                    // ISO-8859-1 symbols
                    "&nbsp;&iexcl;&cent;&pound;&curren;&yen;&brvbar;&sect;&uml;&copy;&ordf;&laquo;&not;&shy;&reg;&macr;&deg;&plusmn;" +
                    "&sup2;&sup3;&acute;&micro;&para;&middot;&cedil;&sup1;&ordm;&raquo;&frac14;&frac12;&frac34;&iquest;" +

                    // Letters with accents (Latin Extended-A)
                    "&Aacute;&aacute;&Eacute;&eacute;&Iacute;&iacute;&Oacute;&oacute;&Uacute;&uacute;&Yacute;&yacute;" +
                    "&Agrave;&agrave;&Egrave;&egrave;&Igrave;&igrave;&Ograve;&ograve;&Ugrave;&ugrave;" +
                    "&Acirc;&acirc;&Ecirc;&ecirc;&Icirc;&icirc;&Ocirc;&ocirc;&Ucirc;&ucirc;" +
                    "&Auml;&auml;&Euml;&euml;&Iuml;&iuml;&Ouml;&ouml;&Uuml;&uuml;&Ccedil;&ccedil;&Ntilde;&ntilde;" +

                    // Mathematical and logical symbols
                    "&forall;&part;&exist;&empty;&nabla;&isin;&notin;&ni;&prod;&sum;&minus;&lowast;&radic;&prop;&infin;" +
                    "&ang;&and;&or;&cap;&cup;&int;&there4;&sim;&cong;&asymp;&ne;&equiv;&le;&ge;" +

                    // Arrows
                    "&larr;&uarr;&rarr;&darr;&harr;&crarr;&lArr;&uArr;&rArr;&dArr;&hArr;" +

                    // Greek letters
                    "&alpha;&beta;&gamma;&delta;&epsilon;&zeta;&eta;&theta;&iota;&kappa;&lambda;&mu;&nu;&xi;&omicron;&pi;&rho;&sigma;" +
                    "&tau;&upsilon;&phi;&chi;&psi;&omega;" +
                    "&Gamma;&Delta;&Theta;&Lambda;&Xi;&Pi;&Sigma;&Phi;&Psi;&Omega;" +

                    // Decimal numeric equivalents
                    "&#60;&#62;&#38;&#34;&#39;" +     // < > & " '
                    "&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;" +
                    "&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;" +

                    // Hexadecimal numeric equivalents
                    "&#x3C;&#x3E;&#x26;&#x22;&#x27;" + // < > & " '
                    "&#xA0;&#xA1;&#xA2;&#xA3;&#xA4;&#xA5;&#xA6;&#xA7;&#xA8;&#xA9;&#xAA;&#xAB;&#xAC;&#xAD;&#xAE;&#xAF;" +
                    "&#xB0;&#xB1;&#xB2;&#xB3;&#xB4;&#xB5;&#xB6;&#xB7;&#xB8;&#xB9;&#xBA;&#xBB;&#xBC;&#xBD;&#xBE;&#xBF;" +

                    // Invalid entities
                    "&unknown;&toolongentitytobegood;&nothing";

    // Expected output string after unescaping the HTML input
    private static final String EXPECTED_OUTPUT =
            "<>&\"' ¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿" +
                    "ÁáÉéÍíÓóÚúÝýÀàÈèÌìÒòÙùÂâÊêÎîÔôÛûÄäËëÏïÖöÜüÇçÑñ" +
                    "∀∂∃∅∇∈∉∋∏∑−∗√∝∞∠∧∨∩∪∫∴∼≅≈≠≡≤≥" +
                    "←↑→↓↔↵⇐⇑⇒⇓⇔" +
                    "αβγδεζηθικλμνξοπρστυφχψωΓΔΘΛΞΠΣΦΨΩ" +
                    "<>&\"' ¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿" +
                    "<>&\"' ¡¢£¤¥¦§¨©ª«¬\u00AD®¯°±²³´µ¶·¸¹º»¼½¾¿" +
                    "&unknown;&toolongentitytobegood;&nothing";

    // Test the behavior of the unescapeHtmlAndXMLEntities method using complex HTML input
    @Test
    public void testUnescapeHtmlAndXMLEntities() {
        String result = HtmlAndXmlEscape.unescapeHtmlAndXMLEntities(HTML_INPUT);
        assertThat(result).isEqualTo(EXPECTED_OUTPUT);
    }

    @Test
    public void testEscapeXML() {
        // Comprehensive input string
        String input = ""
                + "Valid text with <special> characters & symbols.\n"                  // Newline + special characters
                + "Control characters: \u0001\u0002\u0007\u001F should be removed,\t but not tabs or CR.\r\n"    // Invalid low control characters
                + "High invalid: \u0085\u0090\u009F should be removed.\n"                     // Invalid high control characters
                + "Valid emoji: 😀 (surrogate pair: \uD83D\uDE00).\n"                    // Valid surrogate pair
                + "Invalid surrogate: lone high \uD83D or low \uDE00.\n"                 // Invalid surrogates
                + "Tabs\tand carriage returns\r should be preserved.\n"                 // Mixed whitespace
                + "Nested tags: <parent><child>Content</child></parent>\n"               // Nested XML-like content
                + "Pre-escaped data: &lt;already escaped&gt; &amp; should stay.\n"       // Already escaped data
                + "Quotes: 'single' and \"double\" should be escaped.\n";                // Quotes

        // Expected output string
        String expectedOutput = ""
                + "Valid text with &lt;special&gt; characters &amp; symbols.\n"          // Escaped special entities
                + "Control characters:  should be removed,\t but not tabs or CR.\r\n"                            // Invalid low control chars removed
                + "High invalid: \u0085 should be removed.\n"                                       // Invalid high control chars removed
                + "Valid emoji: 😀 (surrogate pair: \uD83D\uDE00).\n"                    // Surrogate pairs preserved
                + "Invalid surrogate: lone high  or low .\n"                             // Lone surrogates removed
                + "Tabs\tand carriage returns\r should be preserved.\n"                 // Tabs and carriage returns preserved
                + "Nested tags: &lt;parent&gt;&lt;child&gt;Content&lt;/child&gt;&lt;/parent&gt;\n" // Nested tags escaped
                + "Pre-escaped data: &amp;lt;already escaped&amp;gt; &amp;amp; should stay.\n"       // Already escaped are also escaped
                + "Quotes: &apos;single&apos; and &quot;double&quot; should be escaped.\n"; // Quotes escaped

        // Execute method
        String result = HtmlAndXmlEscape.escapeXml(input);

        // Assert result
        assertThat(result).isEqualTo(expectedOutput);
    }
}
