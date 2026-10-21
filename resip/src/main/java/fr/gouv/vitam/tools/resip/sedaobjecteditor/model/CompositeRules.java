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
package fr.gouv.vitam.tools.resip.sedaobjecteditor.model;

import fr.gouv.vitam.tools.sedalib.core.AbstractUnitaryDataObject;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListInterface;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.ComplexListMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleMetadataKind;
import fr.gouv.vitam.tools.sedalib.metadata.namedtype.RuleType;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What can be added to an edited object holding other ones, and where: the fields of a complex
 * metadata, of a rule or of a unitary DataObject, the parts of an ArchiveUnit and the DataObjects
 * of a DataObjectGroup.
 * <p>
 * The edited object is described by the tags of the objects it holds, in their order.
 */
public final class CompositeRules {

    /**
     * The tag of a free XML field, for the expandable metadata.
     */
    public static final String ANY_XML = "AnyXMLType";

    /**
     * The tag of a rule identifier, which starts a rule block.
     */
    public static final String RULE = "Rule";

    /**
     * The parts of an ArchiveUnit, in their order.
     */
    public static final List<String> ARCHIVE_UNIT_PARTS = List.of("ArchiveUnitProfile", "Content", "Management");

    private static final String BINARY_DATA_OBJECT = "BinaryDataObject";
    private static final String PHYSICAL_DATA_OBJECT = "PhysicalDataObject";
    private static final String LOG_BOOK = "LogBook";

    private CompositeRules() {}

    // the fields set apart: the identifiers given by the system, and the members of the rule blocks,
    // added to a rule
    private static boolean isSetApart(ComplexListInterface owner, Map.Entry<String, ComplexListMetadataKind> field) {
        if ((owner instanceof AbstractUnitaryDataObject) && field.getKey().endsWith("SystemId")) return true;
        return (owner instanceof RuleType) && (field.getValue() instanceof RuleMetadataKind);
    }

    /**
     * The fields that can be added: the repeatable ones and the absent ones, in schema order, then
     * {@link #ANY_XML} if the owner is expandable.
     *
     * @param owner       the complex metadata, rule or unitary DataObject
     * @param presentTags the tags of the fields it holds
     * @return the tags of the fields that can be added
     * @throws SEDALibException if the fields of the owner can't be known
     */
    public static List<String> addableFields(ComplexListInterface owner, List<String> presentTags)
        throws SEDALibException {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, ComplexListMetadataKind> field : owner.getMetadataMap().entrySet()) {
            if (isSetApart(owner, field)) continue;
            if (field.getValue().isMany() || !presentTags.contains(field.getKey())) result.add(field.getKey());
        }
        if (!owner.isNotExpandable()) result.add(ANY_XML);
        return result;
    }

    /**
     * Whether a field can be present more than once.
     *
     * @param owner the complex metadata, rule or unitary DataObject
     * @param tag   the tag of the field
     * @return true for a repeatable field, and for a field out of the schema of an expandable owner
     * @throws SEDALibException if the fields of the owner can't be known
     */
    public static boolean isRepeatable(ComplexListInterface owner, String tag) throws SEDALibException {
        ComplexListMetadataKind kind = owner.getMetadataMap().get(tag);
        return kind != null ? kind.isMany() : !owner.isNotExpandable();
    }

    /**
     * Where to insert a field so that the fields keep the schema order: after the fields that come
     * before it, and in place of the present one if it can't be repeated.
     *
     * @param owner       the complex metadata, rule or unitary DataObject
     * @param presentTags the tags of the fields it holds
     * @param tag         the tag of the added field
     * @return the insertion index, at the end for a field out of the schema
     * @throws SEDALibException if the fields of the owner can't be known
     */
    public static int insertionIndex(ComplexListInterface owner, List<String> presentTags, String tag)
        throws SEDALibException {
        if ((owner instanceof RuleType) && RULE.equals(tag)) return newRuleIndex((RuleType) owner, presentTags);
        int addedOrder = owner.indexOfMetadata(tag);
        if (addedOrder == -1) return presentTags.size();
        boolean repeatable = owner.getMetadataMap().get(tag).isMany();
        int index = 0;
        for (String presentTag : presentTags) {
            int presentOrder = owner.indexOfMetadata(presentTag);
            if (
                (!repeatable && (presentOrder == addedOrder)) || (presentOrder == -1) || (presentOrder > addedOrder)
            ) break;
            index++;
        }
        return index;
    }

    /**
     * Where to insert a new rule block in a rule: after the last rule block.
     *
     * @param owner       the rule
     * @param presentTags the tags of the fields it holds
     * @return the insertion index
     * @throws SEDALibException if the fields of the rule can't be known
     */
    public static int newRuleIndex(RuleType owner, List<String> presentTags) throws SEDALibException {
        List<String> ruleBlockTags = owner.getRuleMetadataKindList();
        for (int i = presentTags.size() - 1; i >= 0; i--) {
            if (RULE.equals(presentTags.get(i)) || ruleBlockTags.contains(presentTags.get(i))) return i + 1;
        }
        return 0;
    }

    /**
     * Where to insert a field in a rule block, in schema order after its rule identifier.
     *
     * @param owner       the rule
     * @param presentTags the tags of the fields it holds
     * @param ruleIndex   the index of the rule identifier starting the block
     * @param tag         the tag of the added field
     * @return the insertion index, at the end for a field out of the schema
     * @throws SEDALibException if the fields of the rule can't be known
     */
    public static int ruleBlockInsertionIndex(RuleType owner, List<String> presentTags, int ruleIndex, String tag)
        throws SEDALibException {
        int addedOrder = owner.indexOfMetadata(tag);
        if (addedOrder == -1) return presentTags.size();
        boolean repeatable = owner.getMetadataMap().get(tag).isMany();
        int index = ruleIndex + 1;
        for (String presentTag : presentTags.subList(ruleIndex + 1, presentTags.size())) {
            if (RULE.equals(presentTag)) break;
            int presentOrder = owner.indexOfMetadata(presentTag);
            if (
                (!repeatable && (presentOrder == addedOrder)) || (presentOrder == -1) || (presentOrder > addedOrder)
            ) break;
            index++;
        }
        return index;
    }

    /**
     * The parts that can be added to an ArchiveUnit, each one once.
     *
     * @param presentTags the tags of the parts it holds
     * @return the tags of the absent parts, in their order
     */
    public static List<String> addableArchiveUnitParts(List<String> presentTags) {
        List<String> result = new ArrayList<>(ARCHIVE_UNIT_PARTS);
        result.removeAll(presentTags);
        return result;
    }

    /**
     * The objects that can be added to a DataObjectGroup: DataObjects, and a LogBook if it has none.
     *
     * @param presentTags the tags of the objects it holds, the LogBook being the last one
     * @return the tags of the objects that can be added
     */
    public static List<String> addableDataObjectGroupParts(List<String> presentTags) {
        List<String> result = new ArrayList<>(List.of(BINARY_DATA_OBJECT, PHYSICAL_DATA_OBJECT));
        if (presentTags.isEmpty() || !LOG_BOOK.equals(presentTags.get(presentTags.size() - 1))) result.add(LOG_BOOK);
        return result;
    }

    /**
     * Whether an object can be present more than once in a DataObjectGroup.
     *
     * @param tag the tag of the object
     * @return true but for the LogBook
     */
    public static boolean isRepeatableInDataObjectGroup(String tag) {
        return !LOG_BOOK.equals(tag);
    }

    private static int dataObjectGroupOrder(String tag) {
        if (BINARY_DATA_OBJECT.equals(tag)) return 0;
        if (PHYSICAL_DATA_OBJECT.equals(tag)) return 1;
        return 2;
    }

    /**
     * Where to insert an object in a DataObjectGroup: the BinaryDataObjects, then the
     * PhysicalDataObjects, then the LogBook.
     *
     * @param presentTags the tags of the objects it holds
     * @param tag         the tag of the added object
     * @return the insertion index
     */
    public static int dataObjectGroupInsertionIndex(List<String> presentTags, String tag) {
        int addedOrder = dataObjectGroupOrder(tag);
        int index = 0;
        for (String presentTag : presentTags) {
            if (dataObjectGroupOrder(presentTag) > addedOrder) break;
            index++;
        }
        return index;
    }
}
