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

import java.util.Map;

/**
 * <p>
 * Utility class for unescaping HTML and escaping XML Strings.
 * </p>
 *
 * <p>
 * This class provides methods to ensure strings are properly formatted in XML contents.
 */
public class HtmlAndXmlEscape {

    /**
     * Escapes a given string for XML 1.0 compliance.
     *
     * <p>This method ensures the provided string is safe for inclusion in XML 1.0 documents
     * by replacing special characters with their corresponding XML entities and ignoring
     * characters that are invalid according to the XML 1.0 specification.</p>
     *
     * <p>The following character replacements are made:
     * <ul>
     *   <li>{@code &} becomes {@code &amp;}</li>
     *   <li>{@code <} becomes {@code &lt;}</li>
     *   <li>{@code >} becomes {@code &gt;}</li>
     *   <li>{@code "} becomes {@code &quot;}</li>
     *   <li>{@code '} becomes {@code &apos;}</li>
     * </ul>
     * All other valid characters are retained as-is. Invalid characters, including unpaired
     * surrogate characters and certain control characters, are ignored entirely.</p>
     *
     * <p>Note that the method does not escape characters beyond the scope of XML 1.0 compliance.</p>
     *
     * @param input the string to be escaped, may be {@code null}
     * @return the escaped string, or {@code null} if the input was {@code null}
     */
    public static String escapeXml(String input) {
        if (input == null) return null;

        StringBuilder sb = new StringBuilder(input.length());
        int length = input.length();

        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);

            // Handle valid surrogate pairs
            if (Character.isHighSurrogate(c)) {
                // Ensure a valid low surrogate exists
                if (i + 1 < length && Character.isLowSurrogate(input.charAt(i + 1))) {
                    sb.append(c).append(input.charAt(i + 1));
                    i++; // Skip to the low surrogate
                    continue;
                }
                // If unmatched high surrogate, skip it
                continue;
            }

            // If it's an unmatched low surrogate, skip it
            if (Character.isLowSurrogate(c)) {
                continue;
            }
            // Invalid characters according to the XML 1.0 specification
            if (
                (c <= 0x1F && c != '\t' && c != '\n' && c != '\r') ||
                (c >= 0x7F && c <= 0x84) ||
                (c >= 0x86 && c <= 0x9F)
            ) continue; // Ignore invalid characters

            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    private static final Map<String, Character> UNESCAPE_MAP = Map.ofEntries(
        //ISO8859-1 entities
        Map.entry("nbsp", ' '),
        Map.entry("iexcl", '¡'),
        Map.entry("cent", '¢'),
        Map.entry("pound", '£'),
        Map.entry("curren", '¤'),
        Map.entry("yen", '¥'),
        Map.entry("brvbar", '¦'),
        Map.entry("sect", '§'),
        Map.entry("uml", '¨'),
        Map.entry("copy", '©'),
        Map.entry("ordf", 'ª'),
        Map.entry("laquo", '«'),
        Map.entry("not", '¬'),
        Map.entry("shy", '­'),
        Map.entry("reg", '®'),
        Map.entry("macr", '¯'),
        Map.entry("deg", '°'),
        Map.entry("plusmn", '±'),
        Map.entry("sup2", '²'),
        Map.entry("sup3", '³'),
        Map.entry("acute", '´'),
        Map.entry("micro", 'µ'),
        Map.entry("para", '¶'),
        Map.entry("middot", '·'),
        Map.entry("cedil", '¸'),
        Map.entry("sup1", '¹'),
        Map.entry("ordm", 'º'),
        Map.entry("raquo", '»'),
        Map.entry("frac14", '¼'),
        Map.entry("frac12", '½'),
        Map.entry("frac34", '¾'),
        Map.entry("iquest", '¿'),
        Map.entry("Agrave", 'À'),
        Map.entry("Aacute", 'Á'),
        Map.entry("Acirc", 'Â'),
        Map.entry("Atilde", 'Ã'),
        Map.entry("Auml", 'Ä'),
        Map.entry("Aring", 'Å'),
        Map.entry("AElig", 'Æ'),
        Map.entry("Ccedil", 'Ç'),
        Map.entry("Egrave", 'È'),
        Map.entry("Eacute", 'É'),
        Map.entry("Ecirc", 'Ê'),
        Map.entry("Euml", 'Ë'),
        Map.entry("Igrave", 'Ì'),
        Map.entry("Iacute", 'Í'),
        Map.entry("Icirc", 'Î'),
        Map.entry("Iuml", 'Ï'),
        Map.entry("ETH", 'Ð'),
        Map.entry("Ntilde", 'Ñ'),
        Map.entry("Ograve", 'Ò'),
        Map.entry("Oacute", 'Ó'),
        Map.entry("Ocirc", 'Ô'),
        Map.entry("Otilde", 'Õ'),
        Map.entry("Ouml", 'Ö'),
        Map.entry("times", '×'),
        Map.entry("Oslash", 'Ø'),
        Map.entry("Ugrave", 'Ù'),
        Map.entry("Uacute", 'Ú'),
        Map.entry("Ucirc", 'Û'),
        Map.entry("Uuml", 'Ü'),
        Map.entry("Yacute", 'Ý'),
        Map.entry("THORN", 'Þ'),
        Map.entry("szlig", 'ß'),
        Map.entry("agrave", 'à'),
        Map.entry("aacute", 'á'),
        Map.entry("acirc", 'â'),
        Map.entry("atilde", 'ã'),
        Map.entry("auml", 'ä'),
        Map.entry("aring", 'å'),
        Map.entry("aelig", 'æ'),
        Map.entry("ccedil", 'ç'),
        Map.entry("egrave", 'è'),
        Map.entry("eacute", 'é'),
        Map.entry("ecirc", 'ê'),
        Map.entry("euml", 'ë'),
        Map.entry("igrave", 'ì'),
        Map.entry("iacute", 'í'),
        Map.entry("icirc", 'î'),
        Map.entry("iuml", 'ï'),
        Map.entry("eth", 'ð'),
        Map.entry("ntilde", 'ñ'),
        Map.entry("ograve", 'ò'),
        Map.entry("oacute", 'ó'),
        Map.entry("ocirc", 'ô'),
        Map.entry("otilde", 'õ'),
        Map.entry("ouml", 'ö'),
        Map.entry("divide", '÷'),
        Map.entry("oslash", 'ø'),
        Map.entry("ugrave", 'ù'),
        Map.entry("uacute", 'ú'),
        Map.entry("ucirc", 'û'),
        Map.entry("uuml", 'ü'),
        Map.entry("yacute", 'ý'),
        Map.entry("thorn", 'þ'),
        Map.entry("yuml", 'ÿ'),
        // HTML40 Extended entities
        Map.entry("fnof", 'ƒ'),
        Map.entry("Alpha", 'Α'),
        Map.entry("Beta", 'Β'),
        Map.entry("Gamma", 'Γ'),
        Map.entry("Delta", 'Δ'),
        Map.entry("Epsilon", 'Ε'),
        Map.entry("Zeta", 'Ζ'),
        Map.entry("Eta", 'Η'),
        Map.entry("Theta", 'Θ'),
        Map.entry("Iota", 'Ι'),
        Map.entry("Kappa", 'Κ'),
        Map.entry("Lambda", 'Λ'),
        Map.entry("Mu", 'Μ'),
        Map.entry("Nu", 'Ν'),
        Map.entry("Xi", 'Ξ'),
        Map.entry("Omicron", 'Ο'),
        Map.entry("Pi", 'Π'),
        Map.entry("Rho", 'Ρ'),
        Map.entry("Sigma", 'Σ'),
        Map.entry("Tau", 'Τ'),
        Map.entry("Upsilon", 'Υ'),
        Map.entry("Phi", 'Φ'),
        Map.entry("Chi", 'Χ'),
        Map.entry("Psi", 'Ψ'),
        Map.entry("Omega", 'Ω'),
        Map.entry("alpha", 'α'),
        Map.entry("beta", 'β'),
        Map.entry("gamma", 'γ'),
        Map.entry("delta", 'δ'),
        Map.entry("epsilon", 'ε'),
        Map.entry("zeta", 'ζ'),
        Map.entry("eta", 'η'),
        Map.entry("theta", 'θ'),
        Map.entry("iota", 'ι'),
        Map.entry("kappa", 'κ'),
        Map.entry("lambda", 'λ'),
        Map.entry("mu", 'μ'),
        Map.entry("nu", 'ν'),
        Map.entry("xi", 'ξ'),
        Map.entry("omicron", 'ο'),
        Map.entry("pi", 'π'),
        Map.entry("rho", 'ρ'),
        Map.entry("sigmaf", 'ς'),
        Map.entry("sigma", 'σ'),
        Map.entry("tau", 'τ'),
        Map.entry("upsilon", 'υ'),
        Map.entry("phi", 'φ'),
        Map.entry("chi", 'χ'),
        Map.entry("psi", 'ψ'),
        Map.entry("omega", 'ω'),
        Map.entry("thetasym", 'ϑ'),
        Map.entry("upsih", 'ϒ'),
        Map.entry("piv", 'ϖ'),
        Map.entry("bull", '•'),
        Map.entry("hellip", '…'),
        Map.entry("prime", '′'),
        Map.entry("Prime", '″'),
        Map.entry("oline", '‾'),
        Map.entry("frasl", '⁄'),
        Map.entry("weierp", '℘'),
        Map.entry("image", 'ℑ'),
        Map.entry("real", 'ℜ'),
        Map.entry("trade", '™'),
        Map.entry("alefsym", 'ℵ'),
        Map.entry("larr", '←'),
        Map.entry("uarr", '↑'),
        Map.entry("rarr", '→'),
        Map.entry("darr", '↓'),
        Map.entry("harr", '↔'),
        Map.entry("crarr", '↵'),
        Map.entry("lArr", '⇐'),
        Map.entry("uArr", '⇑'),
        Map.entry("rArr", '⇒'),
        Map.entry("dArr", '⇓'),
        Map.entry("hArr", '⇔'),
        Map.entry("forall", '∀'),
        Map.entry("part", '∂'),
        Map.entry("exist", '∃'),
        Map.entry("empty", '∅'),
        Map.entry("nabla", '∇'),
        Map.entry("isin", '∈'),
        Map.entry("notin", '∉'),
        Map.entry("ni", '∋'),
        Map.entry("prod", '∏'),
        Map.entry("sum", '∑'),
        Map.entry("minus", '−'),
        Map.entry("lowast", '∗'),
        Map.entry("radic", '√'),
        Map.entry("prop", '∝'),
        Map.entry("infin", '∞'),
        Map.entry("ang", '∠'),
        Map.entry("and", '∧'),
        Map.entry("or", '∨'),
        Map.entry("cap", '∩'),
        Map.entry("cup", '∪'),
        Map.entry("int", '∫'),
        Map.entry("there4", '∴'),
        Map.entry("sim", '∼'),
        Map.entry("cong", '≅'),
        Map.entry("asymp", '≈'),
        Map.entry("ne", '≠'),
        Map.entry("equiv", '≡'),
        Map.entry("le", '≤'),
        Map.entry("ge", '≥'),
        Map.entry("sub", '⊂'),
        Map.entry("sup", '⊃'),
        Map.entry("nsub", '⊄'),
        Map.entry("sube", '⊆'),
        Map.entry("supe", '⊇'),
        Map.entry("oplus", '⊕'),
        Map.entry("otimes", '⊗'),
        Map.entry("perp", '⊥'),
        Map.entry("sdot", '⋅'),
        Map.entry("lceil", '⌈'),
        Map.entry("rceil", '⌉'),
        Map.entry("lfloor", '⌊'),
        Map.entry("rfloor", '⌋'),
        Map.entry("lang", '〈'),
        Map.entry("rang", '〉'),
        Map.entry("loz", '◊'),
        Map.entry("spades", '♠'),
        Map.entry("clubs", '♣'),
        Map.entry("hearts", '♥'),
        Map.entry("diams", '♦'),
        Map.entry("OElig", 'Œ'),
        Map.entry("oelig", 'œ'),
        Map.entry("Scaron", 'Š'),
        Map.entry("scaron", 'š'),
        Map.entry("Yuml", 'Ÿ'),
        Map.entry("circ", 'ˆ'),
        Map.entry("tilde", '˜'),
        Map.entry("ensp", ' '),
        Map.entry("emsp", ' '),
        Map.entry("thinsp", ' '),
        Map.entry("zwnj", '‌'),
        Map.entry("zwj", '‍'),
        Map.entry("lrm", '‎'),
        Map.entry("rlm", '‏'),
        Map.entry("ndash", '–'),
        Map.entry("mdash", '—'),
        Map.entry("lsquo", '‘'),
        Map.entry("rsquo", '’'),
        Map.entry("sbquo", '‚'),
        Map.entry("ldquo", '“'),
        Map.entry("rdquo", '”'),
        Map.entry("bdquo", '„'),
        Map.entry("dagger", '†'),
        Map.entry("Dagger", '‡'),
        Map.entry("permil", '‰'),
        Map.entry("lsaquo", '‹'),
        Map.entry("rsaquo", '›'),
        Map.entry("euro", '€'),
        // XML entities
        Map.entry("quot", '"'),
        Map.entry("amp", '&'),
        Map.entry("lt", '<'),
        Map.entry("gt", '>'),
        Map.entry("apos", '\'')
    );

    /**
     * Unescapes a string containing HTML or XML entities into a string with the associated Unicode characters.
     *
     * <p>This method supports both Named and Numeric entities (decimal and hexadecimal). For example:
     * <ul>
     * <li>{@code "&lt;" → "<"}</li>
     * <li>{@code "&amp;" → "&"}</li>
     * <li>{@code "&#60;" → "<"}</li>
     * <li>{@code "&#x3C;" → "<"}</li>
     * </ul>
     *
     * <p>If an entity is unrecognized or malformed, it is left unmodified in the output. For example:
     * <ul>
     * <li>{@code "&unknown;" → "&unknown;"}</li>
     * <li>{@code "&toolongentitytobegood;" → "&toolongentitytobegood;"}</li>
     * </ul>
     *
     * @param input the input string potentially containing HTML or XML entities, may be null
     * @return the unescaped string, or {@code null} if the input is null
     */
    public static String unescapeHtmlAndXMLEntities(String input) {
        if (input == null || !input.contains("&")) return input;

        StringBuilder out = new StringBuilder(input.length());
        int len = input.length();

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c != '&') {
                out.append(c);
                continue;
            }

            int semi = input.indexOf(';', i + 1);
            if (semi == -1 || semi - i > 10) {
                out.append('&');
                continue;
            }

            String entity = input.substring(i + 1, semi);
            String full = input.substring(i, semi + 1);

            if (entity.startsWith("#")) {
                try {
                    int codePoint = entity.startsWith("#x") || entity.startsWith("#X")
                        ? Integer.parseInt(entity.substring(2), 16)
                        : Integer.parseInt(entity.substring(1));
                    out.appendCodePoint(codePoint); // Java 11-friendly
                } catch (Exception e) {
                    out.append(full); // on laisse tel quel
                }
                i = semi;
            } else {
                Character repl = UNESCAPE_MAP.get(entity);
                if (repl != null) {
                    out.append(repl);
                } else {
                    out.append(full); // entité inconnue
                }
                i = semi;
            }
        }

        return out.toString();
    }

    /**
     * Private constructor to prevent instantiation of the HTMLAndXMLEscape class,
     * as it is only meant to provide static utility functions.
     */
    private HtmlAndXmlEscape() {}
}
