package fr.gouv.vitam.tools.sedalib.metadata;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import fr.gouv.vitam.tools.sedalib.utils.SEDALibException;

class ManagementTest {

	@Test
	void testConstructors() throws SEDALibException {

		// Given
		Management m = new Management();

		// When loaded with all different kind of metadata

		// Test StorageRule and RuleType subclass newmetadata
		m.addNewMetadata("StorageRule","TestStoRule1",LocalDate.of(1970,1,1),"Transfer");

		// Test AppraisalRule and RuleType subclass metadata add
		AppraisalRule appraisalRule = new AppraisalRule();
		appraisalRule.addRule("TestAppRule1", LocalDate.of(1970,1,1));
		appraisalRule.setPreventInheritance(true);
		appraisalRule.addRule("TestAppRule2");
		appraisalRule.addRefNonRuleId("TestAppRule3");
		appraisalRule.setFinalAction("Keep");
		m.addMetadata(appraisalRule);

		// Test AccessRule and RuleType subclass metadata add
		AccessRule accessRule = new AccessRule();
		accessRule.addRule("TestAccRule1", LocalDate.of(1970,1,1));
		accessRule.setPreventInheritance(true);
		accessRule.addRule("TestAccRule2");
		accessRule.addRefNonRuleId("TestAccRule3");
		m.addMetadata(accessRule);

		// Test DisseminationRule and RuleType subclass metadata add
		DisseminationRule disseminationRule = new DisseminationRule();
		disseminationRule.addRule("TestDisRule1", LocalDate.of(1970,1,1));
		disseminationRule.setPreventInheritance(true);
		disseminationRule.addRule("TestDisRule2");
		disseminationRule.addRefNonRuleId("TestDisRule3");
		m.addMetadata(disseminationRule);

		// Test ReuseRule and RuleType subclass metadata add
		ReuseRule reuseRule = new ReuseRule();
		reuseRule.addRule("TestReuRule1", LocalDate.of(1970,1,1));
		reuseRule.setPreventInheritance(true);
		reuseRule.addRule("TestReuRule2");
		reuseRule.addRefNonRuleId("TestReuRule3");
		m.addMetadata(reuseRule);

		// Test ClassificationRule and RuleType subclass metadata add
		ClassificationRule classificationRule = new ClassificationRule();
		classificationRule.addRule("TestRule1", LocalDate.of(1970,1,1));
		classificationRule.setPreventInheritance(true);
		classificationRule.addRule("TestRule2");
		classificationRule.addRefNonRuleId("TestRule3");
		classificationRule.setClassificationLevel("TestCD");
		classificationRule.setClassificationOwner("TestOwner");
		classificationRule.setClassificationReassessingDate(LocalDate.of(1970,1,1));
		classificationRule.setNeedReassessingAuthorization(true);
		m.addMetadata(classificationRule);

		// Test GenerixXMLBlock
		m.addNewMetadata("LogBook","<LogBook>TestLogBook</LogBook>");

		// Test UpdateOperation
		UpdateOperation updateOperation = new UpdateOperation();
		updateOperation.setSystemId("TestSystemId");
		m.addMetadata(updateOperation);
		m.addNewMetadata("UpdateOperation","TestMetadataName", "TestMetadataValue"); // verify that uniq metadata is overwritten

		String mOut = m.toString();
 //       System.out.println("Value to verify=" + mOut);

		// Test read write in XML string format
		Management mNext = (Management) SEDAMetadata.fromString(mOut, Management.class);
		String mNextOut = mNext.toString();

		// Then
		String testOut = "<Management>\n" +
				"  <StorageRule>\n" +
				"    <Rule>TestStoRule1</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <FinalAction>Transfer</FinalAction>\n" +
				"  </StorageRule>\n" +
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
				"    <Rule>TestAccRule2</Rule>\n" +
				"    <PreventInheritance>true</PreventInheritance>\n" +
				"    <RefNonRuleId>TestAccRule3</RefNonRuleId>\n" +
				"  </AccessRule>\n" +
				"  <DisseminationRule>\n" +
				"    <Rule>TestDisRule1</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <Rule>TestDisRule2</Rule>\n" +
				"    <PreventInheritance>true</PreventInheritance>\n" +
				"    <RefNonRuleId>TestDisRule3</RefNonRuleId>\n" +
				"  </DisseminationRule>\n" +
				"  <ReuseRule>\n" +
				"    <Rule>TestReuRule1</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <Rule>TestReuRule2</Rule>\n" +
				"    <PreventInheritance>true</PreventInheritance>\n" +
				"    <RefNonRuleId>TestReuRule3</RefNonRuleId>\n" +
				"  </ReuseRule>\n" +
				"  <ClassificationRule>\n" +
				"    <Rule>TestRule1</Rule>\n" +
				"    <StartDate>1970-01-01</StartDate>\n" +
				"    <Rule>TestRule2</Rule>\n" +
				"    <PreventInheritance>true</PreventInheritance>\n" +
				"    <RefNonRuleId>TestRule3</RefNonRuleId>\n" +
				"    <ClassificationLevel>TestCD</ClassificationLevel>\n" +
				"    <ClassificationOwner>TestOwner</ClassificationOwner>\n" +
				"    <ClassificationReassessingDate>1970-01-01</ClassificationReassessingDate>\n" +
				"    <NeedReassessingAuthorization>true</NeedReassessingAuthorization>\n" +
				"  </ClassificationRule>\n" +
				"  <LogBook>TestLogBook</LogBook>\n" +
				"  <UpdateOperation>\n" +
				"    <ArchiveUnitIdentifierKey>\n" +
				"      <MetadataName>TestMetadataName</MetadataName>\n" +
				"      <MetadataValue>TestMetadataValue</MetadataValue>\n" +
				"    </ArchiveUnitIdentifierKey>\n" +
				"  </UpdateOperation>\n" +
				"</Management>";
		assertThat(mNextOut).isEqualTo(testOut);
	}

	@Test
	void testAddNewMetadata() {
	}

}
