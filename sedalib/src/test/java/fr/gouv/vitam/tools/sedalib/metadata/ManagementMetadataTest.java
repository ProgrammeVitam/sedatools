package fr.gouv.vitam.tools.sedalib.metadata;

import fr.gouv.vitam.tools.sedalib.metadata.management.AppraisalRule;
import fr.gouv.vitam.tools.sedalib.metadata.management.HoldRule;
import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
        mm.addNewMetadata("HoldRule", "HoldRule1", LocalDate.of(1970, 1, 1),
            Collections.singletonMap("HoldEndDate", LocalDate.of(1970, 1, 1)));
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
        String testOut = "<ManagementMetadata>\n" +
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
