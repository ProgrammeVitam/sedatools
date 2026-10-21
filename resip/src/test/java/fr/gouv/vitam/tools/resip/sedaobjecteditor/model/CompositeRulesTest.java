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

import fr.gouv.vitam.tools.sedalib.core.BinaryDataObject;
import fr.gouv.vitam.tools.sedalib.core.DataObjectPackage;
import fr.gouv.vitam.tools.sedalib.metadata.content.Content;
import fr.gouv.vitam.tools.sedalib.metadata.data.FileInfo;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedaschema.SedaVersion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the editors propose to add, and where, without the graphic interface.
 */
class CompositeRulesTest {

    @Test
    void proposesTheRepeatableAndTheAbsentFieldsThenAnyXml() throws Exception {
        Content content = new Content(SedaVersion.V2_1);

        List<String> addable = CompositeRules.addableFields(content, List.of("DescriptionLevel", "Title"));

        // DescriptionLevel can't be repeated, Title can
        assertThat(addable).doesNotContain("DescriptionLevel").contains("Title", "Description");
        assertThat(addable).last().isEqualTo(CompositeRules.ANY_XML);
        assertThat(CompositeRules.addableFields(content, List.of())).contains("DescriptionLevel");
    }

    @Test
    void proposesTheFieldsOfTheVersion() throws Exception {
        // Signature is superseded by SigningInformation in 2.3
        assertThat(CompositeRules.addableFields(new Content(SedaVersion.V2_1), List.of())).contains("Signature");
        assertThat(CompositeRules.addableFields(new Content(SedaVersion.V2_3), List.of()))
            .contains("SigningInformation")
            .doesNotContain("Signature");
    }

    @Test
    void setsApartTheRuleBlockMembersAndTheSystemIdentifiers() throws Exception {
        List<String> holdRuleFields = CompositeRules.addableFields(new HoldRule(SedaVersion.V2_2), List.of());
        BinaryDataObject bdo = new BinaryDataObject(new DataObjectPackage(SedaVersion.V2_3));

        // added to a rule block, not to the rule
        assertThat(holdRuleFields).contains("Rule", "PreventInheritance").doesNotContain("HoldEndDate", "HoldOwner");
        assertThat(CompositeRules.addableFields(bdo, List.of())).noneMatch(tag -> tag.endsWith("SystemId"));
    }

    @Test
    void knowsTheRepeatableFields() throws Exception {
        assertThat(CompositeRules.isRepeatable(new Content(SedaVersion.V2_1), "Title")).isTrue();
        assertThat(CompositeRules.isRepeatable(new Content(SedaVersion.V2_1), "DescriptionLevel")).isFalse();
        // out of the schema: repeatable in an expandable metadata only
        assertThat(CompositeRules.isRepeatable(new Content(SedaVersion.V2_1), "Unknown")).isTrue();
        assertThat(CompositeRules.isRepeatable(new FileInfo(SedaVersion.V2_1), "Unknown")).isFalse();
    }

    @Test
    void insertsAFieldInSchemaOrder() throws Exception {
        Content content = new Content(SedaVersion.V2_1);
        List<String> present = List.of("DescriptionLevel", "Title", "Description");

        // after the present titles, as it can be repeated
        assertThat(CompositeRules.insertionIndex(content, present, "Title")).isEqualTo(2);
        // in place of the present one, as it can't be repeated
        assertThat(CompositeRules.insertionIndex(content, present, "DescriptionLevel")).isZero();
        // after Title, before Description
        assertThat(CompositeRules.insertionIndex(content, present, "FilePlanPosition")).isEqualTo(2);
        assertThat(CompositeRules.insertionIndex(content, present, "Unknown")).isEqualTo(3);
    }

    @Test
    void insertsTheRuleBlocksAndTheirMembers() throws Exception {
        HoldRule holdRule = new HoldRule(SedaVersion.V2_2);
        List<String> present = List.of("Rule", "HoldEndDate", "Rule", "PreventInheritance");

        // a new rule block after the last one
        assertThat(CompositeRules.insertionIndex(holdRule, present, CompositeRules.RULE)).isEqualTo(3);
        assertThat(CompositeRules.newRuleIndex(holdRule, List.of())).isZero();
        // a member in its block, in schema order
        assertThat(CompositeRules.ruleBlockInsertionIndex(holdRule, present, 0, "HoldOwner")).isEqualTo(2);
        assertThat(CompositeRules.ruleBlockInsertionIndex(holdRule, present, 2, "HoldOwner")).isEqualTo(3);
    }

    @Test
    void proposesEachPartOfAnArchiveUnitOnce() {
        assertThat(CompositeRules.addableArchiveUnitParts(List.of())).containsExactly(
            "ArchiveUnitProfile",
            "Content",
            "Management"
        );
        assertThat(CompositeRules.addableArchiveUnitParts(List.of("Content"))).containsExactly(
            "ArchiveUnitProfile",
            "Management"
        );
    }

    @Test
    void ordersTheDataObjectsOfAGroupBeforeItsLogBook() {
        List<String> present = List.of("BinaryDataObject", "PhysicalDataObject", "LogBook");

        assertThat(CompositeRules.addableDataObjectGroupParts(List.of())).containsExactly(
            "BinaryDataObject",
            "PhysicalDataObject",
            "LogBook"
        );
        assertThat(CompositeRules.addableDataObjectGroupParts(present)).doesNotContain("LogBook");
        assertThat(CompositeRules.dataObjectGroupInsertionIndex(present, "BinaryDataObject")).isEqualTo(1);
        assertThat(CompositeRules.dataObjectGroupInsertionIndex(present, "PhysicalDataObject")).isEqualTo(2);
        assertThat(CompositeRules.isRepeatableInDataObjectGroup("LogBook")).isFalse();
        assertThat(CompositeRules.isRepeatableInDataObjectGroup("BinaryDataObject")).isTrue();
    }
}
