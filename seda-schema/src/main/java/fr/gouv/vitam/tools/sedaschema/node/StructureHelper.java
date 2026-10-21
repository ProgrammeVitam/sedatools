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

import fr.gouv.vitam.tools.sedaschema.SedaSchemaException;
import fr.gouv.vitam.tools.sedaschema.model.ComplexTypeDef;
import fr.gouv.vitam.tools.sedaschema.model.ElementDef;
import fr.gouv.vitam.tools.sedaschema.model.Group;
import fr.gouv.vitam.tools.sedaschema.model.Occurrence;
import fr.gouv.vitam.tools.sedaschema.model.Particle;
import fr.gouv.vitam.tools.sedaschema.model.SimpleTypeDef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema driven structuring of {@link SedaNode} trees: what can be added where, in which order,
 * and what a minimal valid instance looks like. This is what an editor needs to offer only legal
 * operations.
 */
public final class StructureHelper {

    /**
     * Depth limit when building default instances, to stop on recursive required content.
     */
    private static final int MAX_DEFAULT_DEPTH = 32;

    private StructureHelper() {}

    /**
     * A child element that can still be added to a node.
     */
    public static final class Candidate {

        private final ElementDef def;
        private final int remaining;

        Candidate(ElementDef def, int remaining) {
            this.def = def;
            this.remaining = remaining;
        }

        public ElementDef getDef() {
            return def;
        }

        public String getName() {
            return def.getName();
        }

        /**
         * @return how many more can be added, {@link Occurrence#UNBOUNDED} if unlimited
         */
        public int getRemaining() {
            return remaining;
        }

        @Override
        public String toString() {
            return def.getName() + (remaining == Occurrence.UNBOUNDED ? "[*]" : "[" + remaining + "]");
        }
    }

    /**
     * @return the child elements that can still be added to this node given its current children,
     * in schema order. Once an alternative of a choice is used, the other alternatives are no
     * longer proposed.
     */
    public static List<Candidate> addableChildren(SedaNode parent) {
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        ComplexTypeDef ct = parent.getDef() == null ? null : parent.getDef().getComplexType();
        if (ct != null && ct.hasElementContent()) collectCandidates(ct.getContent(), parent, false, candidates);
        return new ArrayList<>(candidates.values());
    }

    private static void collectCandidates(Particle p, SedaNode parent, boolean unbounded, Map<String, Candidate> out) {
        boolean nowUnbounded = unbounded || p.getOccurrence().isUnbounded();
        if (p.isElement()) {
            ElementDef def = p.getElement();
            if (def.isAbstract()) return;
            int count = parent.countChildren(def.getName());
            if (nowUnbounded) out.putIfAbsent(def.getName(), new Candidate(def, Occurrence.UNBOUNDED));
            else if (p.getOccurrence().allows(count)) out.putIfAbsent(
                def.getName(),
                new Candidate(def, p.getOccurrence().getMax() - count)
            );
        } else if (p.isGroup()) {
            Group g = p.getGroup();
            if (g.isChoice() && !nowUnbounded) {
                List<Particle> active = new ArrayList<>();
                for (Particle alt : g.getParticles()) if (isUsed(alt, parent)) active.add(alt);
                for (Particle alt : active.isEmpty() ? g.getParticles() : active) collectCandidates(
                    alt,
                    parent,
                    false,
                    out
                );
            } else {
                for (Particle child : g.getParticles()) collectCandidates(child, parent, nowUnbounded, out);
            }
        }
    }

    private static boolean isUsed(Particle p, SedaNode parent) {
        if (p.isElement()) return parent.countChildren(p.getElement().getName()) > 0;
        if (p.isGroup()) for (Particle c : p.getGroup().getParticles()) if (isUsed(c, parent)) return true;
        return false;
    }

    /**
     * @return the index at which a child of this declaration must be inserted to respect the
     * schema order of the parent content model
     */
    public static int insertionIndex(SedaNode parent, ElementDef def) {
        ComplexTypeDef ct = parent.getDef() == null ? null : parent.getDef().getComplexType();
        if (ct == null) return parent.getChildren().size();
        Map<String, Integer> ranks = new LinkedHashMap<>();
        for (ElementDef e : ct.getChildElements()) ranks.put(e.getName(), ranks.size());
        int rank = ranks.getOrDefault(def.getName(), Integer.MAX_VALUE);
        int index = 0;
        List<SedaNode> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            int childRank = children.get(i).isDeclared()
                ? ranks.getOrDefault(children.get(i).getName(), Integer.MAX_VALUE)
                : Integer.MAX_VALUE;
            if (childRank <= rank) index = i + 1;
        }
        return index;
    }

    /**
     * Creates a child of this declaration with its required content and inserts it at the schema
     * position.
     *
     * @return the new child
     * @throws SedaSchemaException if the declaration is not a child of the parent type or its
     * cardinality is exhausted
     */
    public static SedaNode add(SedaNode parent, ElementDef def) throws SedaSchemaException {
        Candidate candidate = null;
        for (Candidate c : addableChildren(parent)) if (c.getDef() == def) candidate = c;
        if (candidate == null) throw new SedaSchemaException(
            "Impossible d'ajouter [" + def.getName() + "] dans [" + parent.getPath() + "]"
        );
        SedaNode child = createDefault(def);
        parent.addChild(insertionIndex(parent, def), child);
        return child;
    }

    /**
     * Same as {@link #add(SedaNode, ElementDef)} by child name.
     */
    public static SedaNode add(SedaNode parent, String childName) throws SedaSchemaException {
        ComplexTypeDef ct = parent.getDef() == null ? null : parent.getDef().getComplexType();
        ElementDef def = ct == null ? null : ct.getChildElement(childName);
        if (def == null) throw new SedaSchemaException(
            "Élément [" + childName + "] non déclaré dans [" + parent.getPath() + "]"
        );
        return add(parent, def);
    }

    /**
     * @return a minimal instance of the declaration: required attributes with their default or
     * first enumerated value, required children recursively (first alternative of a choice), and
     * the fixed, default or empty text
     */
    public static SedaNode createDefault(ElementDef def) {
        return createDefault(def, 0);
    }

    private static SedaNode createDefault(ElementDef def, int depth) {
        SedaNode node = new SedaNode(def);
        ComplexTypeDef ct = def.getComplexType();
        if (ct != null) {
            ct
                .getAttributes()
                .stream()
                .filter(a -> a.isRequired())
                .forEach(
                    a ->
                        node.setAttribute(
                            a.getName(),
                            firstValue(a.getType(), a.getFixedValue() != null ? a.getFixedValue() : a.getDefaultValue())
                        )
                );
            if (ct.hasElementContent() && depth < MAX_DEFAULT_DEPTH) addRequired(ct.getContent(), node, depth);
        }
        if (def.hasTextContent() && node.getChildren().isEmpty()) node.setText(
            firstValue(def.getTextType(), def.getFixedValue() != null ? def.getFixedValue() : def.getDefaultValue())
        );
        return node;
    }

    private static void addRequired(Particle p, SedaNode parent, int depth) {
        if (!p.getOccurrence().isRequired()) return;
        if (p.isElement()) {
            if (p.getElement().isAbstract()) return;
            for (int i = 0; i < p.getOccurrence().getMin(); i++) parent.addChild(
                createDefault(p.getElement(), depth + 1)
            );
        } else if (p.isGroup()) {
            Group g = p.getGroup();
            if (g.isChoice()) {
                if (!g.getParticles().isEmpty()) addRequired(g.getParticles().get(0), parent, depth);
            } else {
                for (Particle c : g.getParticles()) addRequired(c, parent, depth);
            }
        }
    }

    private static String firstValue(SimpleTypeDef type, String preset) {
        if (preset != null) return preset;
        if (type != null && type.isEnumeration()) return type.getEnumeration().get(0);
        return "";
    }
}
