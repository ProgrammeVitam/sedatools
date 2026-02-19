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
package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.SedaContextExtension;
import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SedaContextExtension.class)
class ManagementMetadataTest {

    @Test
    void testConstructors() throws SEDALibException {
        //Given
        ManagementMetadata mm = new ManagementMetadata();

        // When loaded with all different kind of metadata

        // Test SchemeType metadata
        mm.addNewMetadata("ArchivalProfile", "TestArchivalProfile");

        // Test StringType metadata
        mm.addNewMetadata("OriginatingAgencyIdentifier", "TestOriginatingAgencyIdentifier");
        mm.addNewMetadata("SubmissionAgencyIdentifier", "TestSubmissionAgencyIdentifier");

        // Test RuleType metadata
        mm.addNewMetadata("AccessRule", "TestAccRule1", LocalDate.of(1970, 1, 1));
        AppraisalRule appraisalRule = new AppraisalRule();
        appraisalRule.addRule("TestAppRule1", LocalDate.of(1970, 1, 1));
        appraisalRule.setPreventInheritance(true);
        appraisalRule.addRefNonRuleId("TestAppRule3");
        appraisalRule.setFinalAction("Keep");
        mm.addMetadata(appraisalRule);
        mm.addNewMetadata("AppraisalRule", "TestAppRule2", null);
        mm.addNewMetadata(
            "HoldRule",
            "HoldRule1",
            LocalDate.of(1970, 1, 1),
            Collections.singletonMap("HoldEndDate", LocalDate.of(1970, 1, 1))
        );
        HoldRule holdRule = new HoldRule();
        holdRule.addRule("HoldRule2");
        holdRule.addHoldReason("HoldRule2Reason");
        holdRule.addRule("HoldRule3");
        holdRule.addPreventRearrangement(false);
        mm.addMetadata(holdRule);

        // ...other types all tested in Management metadata

        String mmOut = mm.toString();

        // Test read write in XML string format
        ManagementMetadata mmNext = (ManagementMetadata) SEDAMetadata.fromString(mmOut, ManagementMetadata.class);
        String mmNextOut = mmNext.toString();

        // Then
        String testOut =
            "<ManagementMetadata>\n" +
            "  <ArchivalProfile>TestArchivalProfile</ArchivalProfile>\n" +
            "  <OriginatingAgencyIdentifier>TestOriginatingAgencyIdentifier</OriginatingAgencyIdentifier>\n" +
            "  <SubmissionAgencyIdentifier>TestSubmissionAgencyIdentifier</SubmissionAgencyIdentifier>\n" +
            "  <AppraisalRule>\n" +
            "    <Rule>TestAppRule1</Rule>\n" +
            "    <StartDate>1970-01-01</StartDate>\n" +
            "    <Rule>TestAppRule2</Rule>\n" +
            "    <PreventInheritance>true</PreventInheritance>\n" +
            "    <RefNonRuleId>TestAppRule3</RefNonRuleId>\n" +
            "    <FinalAction>Keep</FinalAction>\n" +
            "  </AppraisalRule>\n" +
            "  <AccessRule>\n" +
            "    <Rule>TestAccRule1</Rule>\n" +
            "    <StartDate>1970-01-01</StartDate>\n" +
            "  </AccessRule>\n" +
            "  <HoldRule>\n" +
            "    <Rule>HoldRule1</Rule>\n" +
            "    <StartDate>1970-01-01</StartDate>\n" +
            "    <HoldEndDate>1970-01-01</HoldEndDate>\n" +
            "    <Rule>HoldRule2</Rule>\n" +
            "    <HoldReason>HoldRule2Reason</HoldReason>\n" +
            "    <Rule>HoldRule3</Rule>\n" +
            "    <PreventRearrangement>false</PreventRearrangement>\n" +
            "  </HoldRule>\n" +
            "</ManagementMetadata>";
        assertThat(mmNextOut).isEqualTo(testOut);
    }
}
