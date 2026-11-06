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
package fr.gouv.vitam.tools.sedaschema.node;

import fr.gouv.vitam.tools.sedaschema.model.AttributeDef;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Group;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Structural validation of a {@link SedaNode} tree against its declarations: content model
 * (order, cardinality, choices), attributes, enumerations and patterns.
 * <p>
 * It is meant for immediate feedback while editing; the reference validation of a manifest
 * remains the full XSD validation by Xerces.
 */
public final class NodeValidator {

    /**
     * A validation problem located at a node.
     */
    public static final class Problem {

        private final String path;
        private final String message;

        Problem(String path, String message) {
            this.path = path;
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return path + ": " + message;
        }
    }

    private static final Set<String> STRING_LIKE = new LinkedHashSet<>(
        List.of("string", "anyURI", "QName", "NOTATION", "hexBinary", "base64Binary")
    );

    private NodeValidator() {}

    /**
     * @return the problems of the whole subtree, empty if structurally valid
     */
    public static List<Problem> validate(SedaNode root) {
        List<Problem> problems = new ArrayList<>();
        validate(root, problems);
        return problems;
    }

    private static void validate(SedaNode node, List<Problem> problems) {
        ElementDef def = node.getDef();
        if (def == null) return; // wildcard content is not checked
        ComplexTypeDef ct = def.getComplexType();
        if (ct != null) {
            validateAttributes(node, ct, problems);
            if (ct.hasElementContent()) validateContent(node, ct, problems);
            else if (!node.getChildren().isEmpty()) problems.add(
                new Problem(node.getPath(), "ne peut pas contenir d'éléments")
            );
        } else if (!node.getChildren().isEmpty()) {
            problems.add(new Problem(node.getPath(), "ne peut pas contenir d'éléments"));
        }
        if (def.hasTextContent()) validateText(node, def, problems);
        else if (node.getText() != null && !node.getText().trim().isEmpty()) problems.add(
            new Problem(node.getPath(), "ne peut pas contenir de texte")
        );
        for (SedaNode child : node.getChildren()) validate(child, problems);
    }

    private static void validateAttributes(SedaNode node, ComplexTypeDef ct, List<Problem> problems) {
        for (AttributeDef a : ct.getAttributes()) {
            String value = node.getAttribute(attributeKey(a));
            if (value == null) {
                if (a.isRequired()) problems.add(
                    new Problem(node.getPath(), "attribut [" + a.getName() + "] obligatoire")
                );
            } else {
                validateValue(node, "attribut [" + a.getName() + "]", value, a.getType(), problems);
                if (a.getFixedValue() != null && !a.getFixedValue().equals(value)) problems.add(
                    new Problem(
                        node.getPath(),
                        "attribut [" + a.getName() + "] doit valoir [" + a.getFixedValue() + "]"
                    )
                );
            }
        }
        if (ct.hasAttributeWildcard()) return;
        for (String name : node.getAttributes().keySet()) {
            if (name.startsWith("xml:") || name.startsWith("xsi:") || name.startsWith("xmlns")) continue;
            boolean declared = false;
            for (AttributeDef a : ct.getAttributes()) if (attributeKey(a).equals(name)) declared = true;
            if (!declared) problems.add(new Problem(node.getPath(), "attribut [" + name + "] non déclaré"));
        }
    }

    private static String attributeKey(AttributeDef a) {
        if ("http://www.w3.org/XML/1998/namespace".equals(a.getNamespace())) return "xml:" + a.getName();
        if ("http://www.w3.org/1999/xlink".equals(a.getNamespace())) return "xlink:" + a.getName();
        return a.getName();
    }

    private static void validateText(SedaNode node, ElementDef def, List<Problem> problems) {
        if (!node.getChildren().isEmpty()) return; // mixed content, text not constrained
        String text = node.getText() == null ? "" : node.getText();
        if (def.getFixedValue() != null && !def.getFixedValue().equals(text)) problems.add(
            new Problem(node.getPath(), "doit valoir [" + def.getFixedValue() + "]")
        );
        validateValue(node, "valeur", text, def.getTextType(), problems);
    }

    private static void validateValue(
        SedaNode node,
        String what,
        String value,
        SimpleTypeDef type,
        List<Problem> problems
    ) {
        if (type == null) return;
        if (type.getVariety() == SimpleTypeDef.Variety.UNION) {
            for (SimpleTypeDef member : type.getMemberTypes()) {
                List<Problem> memberProblems = new ArrayList<>();
                validateValue(node, what, value, member, memberProblems);
                if (memberProblems.isEmpty()) return;
            }
            problems.add(new Problem(node.getPath(), what + " [" + value + "] ne correspond à aucun type de l'union"));
            return;
        }
        if (type.getVariety() == SimpleTypeDef.Variety.LIST) {
            for (String item : value.trim().split("\\s+")) if (!item.isEmpty()) validateValue(
                node,
                what,
                item,
                type.getItemType(),
                problems
            );
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            if (
                type.getPrimitive() != null && !STRING_LIKE.contains(type.getPrimitive()) && !type.isEnumeration()
            ) problems.add(new Problem(node.getPath(), what + " vide pour un type " + type.getPrimitive()));
            else if (type.isEnumeration()) problems.add(
                new Problem(node.getPath(), what + " vide, une valeur de l'énumération est attendue")
            );
            return;
        }
        if (type.isEnumeration() && !type.getEnumeration().contains(trimmed)) {
            problems.add(
                new Problem(node.getPath(), what + " [" + trimmed + "] hors énumération " + type.getEnumeration())
            );
        }
        for (String pattern : type.getPatterns()) {
            try {
                if (!Pattern.compile("^(?:" + pattern + ")$").matcher(trimmed).matches()) problems.add(
                    new Problem(node.getPath(), what + " [" + trimmed + "] ne respecte pas le motif [" + pattern + "]")
                );
            } catch (PatternSyntaxException e) {
                // an XSD regex feature unsupported by java.util.regex is not checked
            }
        }
    }

    // Content model matching

    private static void validateContent(SedaNode node, ComplexTypeDef ct, List<Problem> problems) {
        List<SedaNode> children = node.getChildren();
        Match match = new Match();
        int end = matchParticle(ct.getContent(), children, 0, match);
        if (end < 0) {
            problems.add(new Problem(node.getPath(), "élément attendu : " + match.expectedAt(children)));
        } else if (end < children.size()) {
            problems.add(
                new Problem(
                    node.getPath(),
                    "élément [" +
                    children.get(end).getName() +
                    "] inattendu en position " +
                    (end + 1) +
                    ", attendu : " +
                    match.expectedAt(children)
                )
            );
        }
    }

    /**
     * Tracks the furthest position reached and what was expected there, for error messages.
     */
    private static final class Match {

        int furthest = 0;
        final Set<String> expected = new LinkedHashSet<>();

        void expect(int pos, String name) {
            if (pos > furthest) {
                furthest = pos;
                expected.clear();
            }
            if (pos == furthest) expected.add(name);
        }

        String expectedAt(List<SedaNode> children) {
            String where = furthest < children.size() ? "avant [" + children.get(furthest).getName() + "]" : "en fin";
            return expected + " " + where;
        }
    }

    /**
     * Greedy matching, which is exact for the deterministic content models XSD requires.
     *
     * @return the position after the particle, or -1 if its minimum occurrence isn't met
     */
    private static int matchParticle(Particle p, List<SedaNode> children, int pos, Match match) {
        int count = 0;
        int cur = pos;
        while (p.getOccurrence().allows(count)) {
            int next = matchTerm(p, children, cur, match);
            if (next < 0) break;
            count++;
            if (next == cur) break; // matched nothing, don't loop
            cur = next;
        }
        return count < p.getOccurrence().getMin() ? -1 : cur;
    }

    private static int matchTerm(Particle p, List<SedaNode> children, int pos, Match match) {
        if (p.isElement()) {
            String name = p.getElement().getName();
            if (
                pos < children.size() && children.get(pos).isDeclared() && children.get(pos).getName().equals(name)
            ) return pos + 1;
            match.expect(pos, name);
            return -1;
        }
        if (p.isWildcard()) {
            if (
                pos < children.size() &&
                !children.get(pos).isDeclared() &&
                p.getWildcard().accepts(children.get(pos).getNamespace())
            ) {
                return pos + 1;
            }
            match.expect(pos, "*");
            return -1;
        }
        Group g = p.getGroup();
        switch (g.getCompositor()) {
            case SEQUENCE: {
                int cur = pos;
                for (Particle c : g.getParticles()) {
                    cur = matchParticle(c, children, cur, match);
                    if (cur < 0) return -1;
                }
                return cur;
            }
            case CHOICE: {
                boolean emptyMatch = false;
                for (Particle alt : g.getParticles()) {
                    int next = matchParticle(alt, children, pos, match);
                    if (next > pos) return next;
                    if (next == pos) emptyMatch = true;
                }
                return emptyMatch ? pos : -1;
            }
            case ALL:
            default: {
                int cur = pos;
                boolean[] used = new boolean[g.getParticles().size()];
                boolean progress = true;
                while (progress) {
                    progress = false;
                    for (int i = 0; i < used.length; i++) {
                        if (used[i]) continue;
                        int next = matchTerm(g.getParticles().get(i), children, cur, match);
                        if (next > cur) {
                            used[i] = true;
                            cur = next;
                            progress = true;
                        }
                    }
                }
                for (int i = 0; i < used.length; i++) if (
                    !used[i] && g.getParticles().get(i).getOccurrence().isRequired()
                ) return -1;
                return cur;
            }
        }
    }
}
